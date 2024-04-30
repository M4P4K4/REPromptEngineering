import csv
import os
import uuid

from openai import OpenAI
from dotenv import load_dotenv
from pathlib import Path
from enum import Enum

from openai.types.chat import ChatCompletion

from read_gsheet import GSheetReader


class Game(Enum):
    DICE = "dice"
    SCOPA = "scopa"


class GPTModel(Enum):
    GPT_3 = "gpt-3.5-turbo-16k"
    GPT_4 = "gpt-4"


# store your keys in "variables.env"
load_dotenv(dotenv_path=Path("../../../REPromptEngineering/variables.env"))
API_KEY = os.getenv("API_KEY")
ORG_KEY = os.getenv("ORG_KEY")
client = OpenAI(organization=ORG_KEY, api_key=API_KEY)


# list of all available parameters: https://platform.openai.com/docs/api-reference/chat/create
def main():
    # task2_analyze_tracing(Game.DICE, "test")
    smells = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21]
    print(get_all_requirements(smells, Game.DICE))

    # task2_trace_requirements(Game.DICE, "", GPTModel.GPT_4)


def write_output_to_files(game: Game, uid: str, prompt: str, output: ChatCompletion, smells=None):
    if not smells:
        smells = []

    sub_dir = "../../outputs/" + game.value
    # write output
    with open(sub_dir + "/output/output_" + uid + ".txt", "x") as f:
        f.write(str(output))
        f.close()

    # write prompt
    with open(sub_dir + "/prompt/prompt_" + uid + ".txt", "x") as f:
        f.write(prompt)
        f.close()

    # write csv
    with open(sub_dir + "/csv/csv_" + uid + ".csv", "x") as f:
        content = output.choices[0].message.content.split("\n")
        header = True
        for line in content:
            if header:
                f.write(line + ",'Was the Rule smellfree?','Expected Line'") # TODO extra Zeilen sollen hier raus
                header = False
            else:
                parts = line.split(",")
                if int(parts[0].replace("'", "").replace('"', '')) in smells:
                    f.write(line + ",'No',")
                else:
                    f.write(line + ",'Yes',")
            f.write("\n")
        f.close()


def prompt_in_chatgpt(given_prompt, model, temperature, max_tokens=None):
    return client.chat.completions.create(
        model=model.value,
        temperature=temperature,
        max_tokens=max_tokens,
        messages=[{
            "role": "user",
            "content": given_prompt
        }],
    )


def get_all_requirements(smells, game):
    gsheet_reader = GSheetReader(game)
    values = gsheet_reader.values

    # create the prompt from results
    prompt = ""
    for row in values:
        if int(row[0]) in smells and row[5]:
            prompt += " \n" + row[0] + ". " + row[5]
        elif row[7]:
            prompt += " \n" + row[0] + ". " + row[7]
    return prompt.rstrip()


def task2_trace_requirements(game: Game, model: GPTModel, temperature=0, smells):
    prompt = create_prompt(game, smells)
    output = prompt_in_chatgpt(prompt, model, temperature)

    write_output_to_files(game, uid, prompt, output, 2)


def create_prompt(game: Game, smells: list[int]):
    code = get_code_with_linenumbering()
    requirements = get_all_requirements(smells, game)

    prompt = "Look at the following requirements and the code provided below. Write a CSV file with the following columns: 'Rule ID', 'Is it implemented?', 'Lines of implementation in source code' where the last column should be left empty if not correctly implemented.\n"
    prompt += "The code is:\n"
    prompt += code
    prompt += "The requirements are:\n"
    prompt += requirements
    return prompt


def get_code_with_linenumbering():
    code = ""
    with open("../../src/Code Output/src/groundTruth/DiceGame.java") as codefile:
        idx = 1
        for line in codefile:
            line = line.split("//")  # removes every comment
            code += str(idx) + ". " + line[0]
            if len(line) > 1:
                code += "\n"
            idx += 1
        codefile.close()
    return code


def compare_lines(real_data, prediction, amount_lines):
    lines_real_data = get_all_lines(real_data)
    lines_prediction = get_all_lines(prediction)

    true_positives = 0
    true_negatives = amount_lines - len(lines_prediction)
    false_positives = len(lines_prediction)
    false_negatives = 0

    for line in lines_real_data:
        if line in lines_prediction:
            true_positives += 1
            false_positives -= 1
        else:
            false_negatives += 1
            true_negatives -= 1

    precision = true_positives / (true_positives + false_positives)
    recall = true_positives / (true_positives + false_negatives)

    return [precision, recall]


def get_all_lines(lines):
    lines = lines.split(",")
    all_lines = []

    for line in lines:
        line = line.replace("'", "").replace('"', '').strip()
        if "-" in line:
            all_lines.extend(list(range(int(line.split("-")[0]), int(line.split("-")[1]) + 1)))
        else:
            all_lines.append(int(line))

    return all_lines


def task2_analyze_tracing(game: Game, uid: str):
    fieldnames = ['Rule ID', 'Is it implemented?', 'Lines of implementation in source code']

    with (open("../../cases/groundTruthTracing_DiceGame.csv") as csv_file1,
          open("../../outputs_task2/" + game.value + "/csv/csv_" + uid + ".csv") as csv_file2):
        reader_file1 = list(
            csv.DictReader(csv_file1, fieldnames=fieldnames, delimiter=",", skipinitialspace=True, quotechar="'"))
        reader_file2 = list(
            csv.DictReader(csv_file2, fieldnames=fieldnames, delimiter=",", skipinitialspace=True, quotechar="'"))
        if len(reader_file2) == len(reader_file1):
            for num, actual_lines in enumerate(reader_file1):
                if num != 0:
                    answer_line = "Rule " + actual_lines[fieldnames[0]] + ": "
                    predicted_lines = reader_file2[num]
                    if actual_lines[fieldnames[0]] == predicted_lines[fieldnames[0]]:
                        answer_line += "\tIdentical Rules |"

                    if actual_lines[fieldnames[1]] == predicted_lines[fieldnames[1]]:
                        answer_line += "\tCorrect implementation prediction |"
                    else:
                        answer_line += "\tIncorrect implementation prediction |"

                    precision_recall = compare_lines(actual_lines[fieldnames[2]], predicted_lines[fieldnames[2]], 87)
                    answer_line += "\tprecision: " + str(round(precision_recall[0], 2)) + "\trecall: " + str(
                        round(precision_recall[1], 2))

                    print(answer_line)


if __name__ == '__main__':
    main()
