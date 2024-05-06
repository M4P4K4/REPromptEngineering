import csv
import os
import uuid
import numpy as np
import matplotlib.pyplot as plot

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


AMOUNT_LINES_IN_GROUND_TRUTH_CODE = 143


# store your keys in "variables.env"
load_dotenv(dotenv_path=Path("../../../REPromptEngineering/variables.env"))
API_KEY = os.getenv("API_KEY")
ORG_KEY = os.getenv("ORG_KEY")
client = OpenAI(organization=ORG_KEY, api_key=API_KEY)


# list of all available parameters: https://platform.openai.com/docs/api-reference/chat/create
def main():
    # Step 1: Trace Requirements
    # smells = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21]
    # smells = []
    # task2_trace_requirements(Game.DICE, GPTModel.GPT_4, smells, 0)

    # Step 2: Analyze the tracing & evaluate Performance
    # csv_gt = ["groundTruthTracing_DiceGame_intersection", "groundTruthTracing_DiceGame_unification",
    #           "groundTruthTracing_DiceGame_Alessio", "groundTruthTracing_DiceGame_Chetan"]
    csv_gt = ["groundTruthTracing_DiceGame_unification"]
    for csv_name in csv_gt:
        datasets = []
        print("------------------------------------")
        print(csv_name)
        datasets.append(task2_analyze_tracing(Game.DICE, "b0f6a5fe-fe52-4bba-9ea7-c1101aeff2c2", csv_name))
        datasets.append(task2_analyze_tracing(Game.DICE, "dd830856-88e0-41e4-a28a-8374340a4db8", csv_name))
        datasets.append(task2_analyze_tracing(Game.DICE, "35f8a7f0-bfca-4bfe-b981-fa8631f5f71e", csv_name))
        datasets.append(task2_analyze_tracing(Game.DICE, "4d3578d7-9e7c-4707-98ab-b10be568ec3f", csv_name))
        datasets.append(task2_analyze_tracing(Game.DICE, "d1261cf6-f3c1-47a5-9fe2-eddfdbc1cad0", csv_name))
        # evaluate_performance(datasets)

        values = [evaluate_performance_per_ruleid(datasets, "Precision"),
                  evaluate_performance_per_ruleid(datasets, "Recall")]

        plot.title("Results for LOC tracing of smell-free requirements (RQ1)")
        plot.boxplot(values, labels=["Precision","Recall"], meanline=True, showfliers=True, showmeans=True)
        plot.grid(axis="y", linestyle="--")
        plot.show()


def evaluate_performance(datasets):
    impl_pred = dict(tp=0, tn=0, fp=0, fn=0)
    loc_precision = 0
    loc_recall = 0
    len_datasets = 0

    for dataset in datasets:
        for data in dataset:
            if data['Implemented'] is True and data['Correct predicted'] is True:
                impl_pred.update(tp=impl_pred.get('tp')+1)
            elif data['Implemented'] is False and data['Correct predicted'] is True:
                impl_pred.update(tn=impl_pred.get('tn')+1)
            elif data['Implemented'] is True and data['Correct predicted'] is False:
                impl_pred.update(fn=impl_pred.get('fn')+1)
            elif data['Implemented'] is False and data['Correct predicted'] is False:
                impl_pred.update(fp=impl_pred.get('fp')+1)

            if data['Implemented'] is True:
                loc_precision += data['Precision']
                loc_recall += data['Recall']
                len_datasets += 1

        # len_datasets += len(dataset)

    loc_recall = loc_recall / len_datasets
    loc_precision = loc_precision / len_datasets

    print(impl_pred)
    print()
    print("Implementation Prediction: Precision/Recall")
    print(calculate_precision_recall(impl_pred.get('tp'), impl_pred.get('tn'), impl_pred.get('fp'),  impl_pred.get('fn')))
    print()
    print("LOC Prediction: Precision/Recall/Amount Dataset")
    print([loc_precision, loc_recall, len_datasets])


def evaluate_performance_per_ruleid(datasets, measurement_type):
    d = {
        "1": [-1, -1, -1, -1, -1],
        "2": [-1, -1, -1, -1, -1],
        "3": [-1, -1, -1, -1, -1],
        "4": [-1, -1, -1, -1, -1],
        "5": [-1, -1, -1, -1, -1],
        "6": [-1, -1, -1, -1, -1],
        "7": [-1, -1, -1, -1, -1],
        "8": [-1, -1, -1, -1, -1],
        "9": [-1, -1, -1, -1, -1],
        "10": [-1, -1, -1, -1, -1],
        "11": [-1, -1, -1, -1, -1],
        "12": [-1, -1, -1, -1, -1],
        "13": [-1, -1, -1, -1, -1],
        "15": [-1, -1, -1, -1, -1],
        "16": [-1, -1, -1, -1, -1],
        "17": [-1, -1, -1, -1, -1],
        "18": [-1, -1, -1, -1, -1],
        "19": [-1, -1, -1, -1, -1],
        "20": [-1, -1, -1, -1, -1],
        "21": [-1, -1, -1, -1, -1]
    }
    for idx, dataset in enumerate(datasets):
        for data in dataset:
            if measurement_type in data:
                d[data['Rule ID']][idx] = round(data[measurement_type], 2)

    values = []
    for key, value in d.items():
        if value[0] != -1:
            values = [*values, *value]
        # print(key, value, "Average ", np.mean(value))

    return values


def write_output_to_files(game: Game, uid: str, prompt: str, output: ChatCompletion, smells=None):
    if not smells:
        smells = ''

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
        content = output.choices[0].message.content
        f.write(content)
        f.close()

    with open(sub_dir + "/overview.csv", "a") as f:
        # f.write("'UID', 'Smelly Rules'\n")
        f.write("'" + uid + "', '" + str(smells) + "'\n")


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


def task2_trace_requirements(game: Game, model: GPTModel, smells, temperature=0):
    prompt = create_prompt(game, smells)
    output = prompt_in_chatgpt(prompt, model, temperature)
    uid = str(uuid.uuid4())

    write_output_to_files(game, uid, prompt, output, smells)


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

    return calculate_precision_recall(true_positives, true_negatives, false_positives, false_negatives)


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


def calculate_precision_recall(tp, tn, fp, fn):
    precision = tp / (tp + fp)
    recall = tp / (tp + fn)

    return [precision, recall]


def task2_analyze_tracing(game: Game, uid: str, csv_name: str):
    fieldnames = ['Rule ID', 'Is it implemented?', 'Lines of implementation in source code']
    dataset = []

    with (open("../../cases/" + csv_name + ".csv") as csv_file1,
          open("../../outputs/" + game.value + "/csv/csv_" + uid + ".csv") as csv_file2):
        reader_file1 = list(
            csv.DictReader(csv_file1, fieldnames=fieldnames, delimiter=",", skipinitialspace=True, quotechar="'"))
        reader_file2 = list(
            csv.DictReader(csv_file2, fieldnames=fieldnames, delimiter=",", skipinitialspace=True, quotechar="'"))
        if len(reader_file1) == len(reader_file2) or len(reader_file1) + 1 == len(reader_file2):
            for num, actual_lines in enumerate(reader_file1):
                data = {}
                if num != 0:
                    predicted_lines = reader_file2[num]
                    new_num = num
                    while actual_lines[fieldnames[0]] != predicted_lines[fieldnames[0]]:
                        new_num += 1
                        predicted_lines = reader_file2[new_num]
                    data['Rule ID'] = actual_lines[fieldnames[0]]

                    if actual_lines[fieldnames[1]] == "Yes":
                        data['Implemented'] = True
                    else:
                        data['Implemented'] = False

                    if actual_lines[fieldnames[1]] == predicted_lines[fieldnames[1]]:
                        data['Correct predicted'] = True
                    else:
                        data['Correct predicted'] = False

                    if actual_lines[fieldnames[1]] == "Yes" and predicted_lines[fieldnames[1]] == "Yes":
                        precision_recall = compare_lines(actual_lines[fieldnames[2]], predicted_lines[fieldnames[2]], AMOUNT_LINES_IN_GROUND_TRUTH_CODE)
                        data['Precision'] = precision_recall[0]
                        data['Recall'] = precision_recall[1]

                    dataset.append(data)
    return dataset


if __name__ == '__main__':
    main()
