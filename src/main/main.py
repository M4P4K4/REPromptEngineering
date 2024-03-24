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
    ARKANOID = "arkanoid"
    SNAKE = "snake"
    SCOPA = "scopa"
    PONG = "pong"


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
    # smells = [8, 9, 10, 11, 12, 13, 14, 15, 16] # All smells for Dice
    task1_generate_code([], Game.SCOPA, GPTModel.GPT_4, max_tokens=3300)

    # task2_trace_requirements(Game.DICE, "", GPTModel.GPT_4)


def task1_generate_code(smells: list[int], game: Game, model: GPTModel, temperature=0, max_tokens=None):
    unique_id = str(uuid.uuid4()).replace("-", "_")

    given_prompt = create_prompt_task1(smells, game)
    output = prompt_in_chatgpt(given_prompt, model, temperature, max_tokens)

    write_output_and_prompt_file(game, unique_id, given_prompt, output)
    write_overview_file(game, smells, model, temperature, unique_id)
    create_java_code(game, unique_id, output.choices[0].message.content.split("\n"))


def write_output_and_prompt_file(game: Game, uid: str, prompt: str, output: ChatCompletion, task_nr=1, smells=None):
    if not smells:
        smells = []

    sub_dir = "../../outputs_task" + str(task_nr) + "/" + game.value
    # write output
    with open(sub_dir + "/output/output_" + uid + ".txt", "x") as f:
        f.write(str(output))
        f.close()

    # write prompt
    with open(sub_dir + "/prompt/prompt_" + uid + ".txt", "x") as f:
        f.write(prompt)
        f.close()

    if task_nr == 2:
        # write csv
        with open(sub_dir + "/csv/csv_" + uid + ".csv", "x") as f:
            content = output.choices[0].message.content.split("\n")
            header = True
            for line in content:
                if header:
                    f.write(line + ",'Was the Rule smellfree?','Expected Line'")
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


def create_prompt_task1(smells, game, only_requirements=False):

    gsheet_reader = GSheetReader(game)
    values = gsheet_reader.values

    # create the prompt from results
    prompt = ""
    if not only_requirements:
        prompt += "Create java code for the following description of a game:\n"
    for row in values:
        prompt += row[0] + ". "
        if int(row[0]) in smells and row[5]:
            prompt += row[5] + " \n"
        elif row[6]:
            prompt += row[6] + " \n"
    return prompt.rstrip()


def write_overview_file(game, smells, model, temperature, unique_id):
    overview_filepath = "../../outputs_task1/" + game.value + "/overview_" + game.value + ".csv"
    if os.path.isfile(overview_filepath):
        mode = "a"  # open existing file
    else:
        mode = "x"  # create new file
    with open(overview_filepath, mode, encoding="utf8", newline='') as csvfile:
        fieldnames = ["Ground Truth", "Smelly Rules", "Model", "Temperature", "UUID", "Additional Infos",
                      "Rule 1", "Rule 2", "Rule 3", "Rule 4", "Rule 5", "Rule 6", "Rule 7", "Rule 8", "Rule 9",
                      "Rule 10", "Rule 11", "Rule 12", "Rule 13", "Rule 14", "Rule 15", "Rule 16", "Rule 17"]
        writer = csv.DictWriter(csvfile, delimiter=";", fieldnames=fieldnames)
        smells_string = ""

        for idx, smell in enumerate(smells):
            smells_string += str(smell)
            if len(smells) - 1 > idx:
                smells_string += ", "

        if mode == "x":
            writer.writeheader()  # Only for the first output generation of a game!

        writer.writerow({"Ground Truth": (True if smells else False),
                         "Smelly Rules": smells_string,
                         "Model": model.value,
                         "Temperature": temperature,
                         "UUID": unique_id})
        csvfile.close()


def create_java_code(game: Game, unique_id: uuid, output: list[str]):
    unique_id = unique_id.replace("-", "_")

    if game == Game.DICE:
        old_name = "DiceGame"
        new_name = "DiceGame_" + str(unique_id)
    elif game == Game.SCOPA:
        old_name = "ScopaGame"
        new_name = "ScopaGame_" + str(unique_id)
    else:
        old_name = "TEST"
        new_name = "TEST_"

    code = "package generatedCode." + game.value + ";\n\n"

    is_code = False
    for line in output:
        if line.startswith("```") and not is_code:
            is_code = True
        elif line.startswith("```") and is_code:
            break
        elif is_code:
            code += line.replace(old_name, new_name) + "\n"

    # write code
    with open("../../src/Code Output/src/generatedCode/" + game.value + "/" + new_name + ".java", "x") as f:
        f.write(code)
        f.close()


def task2_trace_requirements(game: Game, uid: str, model: GPTModel, temperature=0):
    smells = get_smells(uid, game)
    prompt = create_prompt_task2(game, uid, smells)
    output = prompt_in_chatgpt(prompt, model, temperature)

    write_output_and_prompt_file(game, uid, prompt, output, 2, smells)


def create_prompt_task2(game: Game, uid: str, smells: list[int]):
    code = ""
    game_name = "DiceGame_" + str(uid)
    with open("../../src/Code Output/src/generatedCode/" + game.value + "/" + game_name + ".java") as codefile:
        idx = 1
        for line in codefile:
            line = line.split("//")  # removes every comment
            code += str(idx) + ". " + line[0]
            if len(line) > 1:
                code += "\n"
            idx += 1
        codefile.close()

    requirements = create_prompt_task1(smells, game, True)

    prompt = "Look at the following requirements and the code provided below. Write a CSV file with the following columns: 'Rule ID', 'Is it implemented?', 'Lines of implementation in source code' where the last column should be left empty if not correctly implemented.\n"
    prompt += "The code is:\n"
    prompt += code
    prompt += "The requirements are:\n"
    prompt += requirements
    return prompt


def get_smells(uid, game: Game):
    overview_filepath = "../../outputs_task1/" + game.value + "/overview_" + game.value + ".csv"
    smells = []
    with open(overview_filepath) as csvfile:
        for line in csvfile:
            line = line.split(";")
            if uid in line[4] and line[1]:
                smells = line[1].split(",")
                break
        smells = [int(s.rstrip()) for s in smells]

    return smells


if __name__ == '__main__':
    main()
