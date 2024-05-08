import csv
import os
import re
import uuid
import numpy as np
import matplotlib.pyplot as plot
import random

from openai import OpenAI
from dotenv import load_dotenv
from pathlib import Path
from enum import Enum

from openai.types.chat import ChatCompletion

from read_gsheet import GSheetReader


class Game(Enum):
    DICE = "dice"
    # SCOPA = "scopa"


class GPTModel(Enum):
    # GPT_3 = "gpt-3.5-turbo-16k"
    GPT_4 = "gpt-4"


AMOUNT_LINES_IN_GROUND_TRUTH_CODE = 143
SUB_DIR = "../../outputs/rq2_all_smells/"

# store your keys in "variables.env"
load_dotenv(dotenv_path=Path("../../../REPromptEngineering/variables.env"))
API_KEY = os.getenv("API_KEY")
ORG_KEY = os.getenv("ORG_KEY")
client = OpenAI(organization=ORG_KEY, api_key=API_KEY)


# list of all available parameters: https://platform.openai.com/docs/api-reference/chat/create
def main():
    # All SMELLS: [8, 9, 10, 11, 14, 15, 16, 17, 18, 19, 20, 21]

    #### RQ 1 ####
    # all_smells = [[]]  # no smells

    #### RQ 2 ####
    # all_smells = [[8], [9], [10], [11], [14], [15], [16], [17], [18], [19], [20], [21]]  # one smell
    all_smells = [[8, 9, 10, 11, 14, 15, 16, 17, 18, 19, 20, 21]]  # all smells

    #### RQ 3 ####
    # all_smells = [[8, 9, 10, 19]]  # lexic
    # all_smells = [[11, 14, 15, 20]]  # semantic
    # all_smells = [[16, 17, 18, 21]]  # syntax

    #### Step 1: Trace Requirements ####
    # for smells in all_smells:
    #     for x in range(5):
    #         task2_trace_requirements(Game.DICE, GPTModel.GPT_4, smells, 0)
    # print(random.choices(smells, k=2))

    #### Step 2: Analyze the tracing & evaluate Performance ####
    # csv_gt = ["groundTruthTracing_DiceGame_Alessio",
    #           "groundTruthTracing_DiceGame_Chetan",
    #           "groundTruthTracing_DiceGame_unification",
    #           "groundTruthTracing_DiceGame_intersection"]
    csv_gt = ["csv_test_test"]
    # csv_gt = ["groundTruthTracing_DiceGame_unification"]
    for csv_name in csv_gt:
        datasets = build_datasets(csv_name, all_smells)
        evaluate_performance(datasets)
        build_boxplot(datasets, all_smells)


def build_boxplot(datasets, smells):
    values = [evaluate_performance_per_ruleid(datasets, "Precision"),
              evaluate_performance_per_ruleid(datasets, "Recall")]

    if smells:
        plot.title("Smelly Rules: " + str(smells))
    plot.boxplot(values, labels=["Precision", "Recall"], meanline=True, showfliers=True, showmeans=True)
    plot.grid(axis="y", linestyle="--")
    plot.show()


def build_datasets(csv_name, smells):
    datasets = []
    print("------------------------------------")
    print(csv_name.split("_")[2].upper())

    with open(SUB_DIR + "overview.csv") as csv_file:
        fn = ["UID", "Smelly Rules", "Additional Infos"]
        reader_file = list(csv.DictReader(csv_file, fieldnames=fn, delimiter=",", skipinitialspace=True, quotechar="'"))
        for num, line in enumerate(reader_file):
            for smell in smells:
                if line[fn[1]] == str(smell) and "error" not in line[fn[2]]:
                    datasets.append(task2_analyze_tracing(line[fn[0]], csv_name))

    return datasets


def evaluate_performance(datasets):
    pred = dict(tp=0, tn=0, fp=0, fn=0)
    loc_precision = 0
    loc_recall = 0
    len_datasets = 0

    for dataset in datasets:
        for data in dataset:
            if data['Implemented'] is True and data['Correct predicted'] is True:
                pred.update(tp=pred.get('tp') + 1)
            elif data['Implemented'] is False and data['Correct predicted'] is True:
                pred.update(tn=pred.get('tn') + 1)
            elif data['Implemented'] is True and data['Correct predicted'] is False:
                pred.update(fn=pred.get('fn') + 1)
            else:
                pred.update(fp=pred.get('fp') + 1)

            if data['Implemented'] is True:
                loc_precision += data['Precision']
                loc_recall += data['Recall']
                len_datasets += 1

        # len_datasets += len(dataset)

    loc_recall = loc_recall / len_datasets
    loc_precision = loc_precision / len_datasets

    # print()
    precision_recall = [round(x, 2) for x in calculate_precision_recall(pred.get('tp'), pred.get('tn'), pred.get('fp'), pred.get('fn'))]
    print("Implementation Prediction: Precision/Recall \t\t", precision_recall)
    # print()
    precision_recall = [round(loc_precision, 2), round(loc_recall, 2)]
    print("LOC Prediction: Precision/Recall/Amount Dataset \t", precision_recall, len_datasets)


def evaluate_performance_per_ruleid(datasets, measurement_type):
    d = {
        "1": [],
        "2": [],
        "3": [],
        "4": [],
        "5": [],
        "6": [],
        "7": [],
        "8": [],
        "9": [],
        "10": [],
        "11": [],
        "12": [],
        "13": [],
        "14": [],
        "15": [],
        "16": [],
        "17": [],
        "18": [],
        "19": [],
        "20": [],
        "21": []
    }

    for idx, dataset in enumerate(datasets):
        for data in dataset:
            if measurement_type in data:
                d[data['Rule ID']].append(round(data[measurement_type], 2))

    values = []
    for key, value in d.items():
        # if value[0] != -1:
        values = [*values, *value]
    # print(key, value, "Average ", np.mean(value))

    return values


def write_output_to_files(uid: str, prompt: str, output: ChatCompletion, smells=None):
    # if not smells:
    #     smells = ''

    # write output
    with open(SUB_DIR + "/output/output_" + uid + ".txt", "x") as f:
        f.write(str(output))
        f.close()

    # write prompt
    with open(SUB_DIR + "/prompt/prompt_" + uid + ".txt", "x") as f:
        f.write(prompt)
        f.close()

    # write csv
    with open(SUB_DIR + "/csv/csv_" + uid + ".csv", "x") as f:
        content = clean_content(output.choices[0].message.content)
        f.write(content)
        f.close()

    error = ""
    if not content:
        error = "error"

    with open(SUB_DIR + "/overview.csv", "a") as f:
        # f.write("'UID', 'Smelly Rules', 'Additional Infos'\n")
        f.write("'" + uid + "', '" + str(smells) + "', '" + error + "'\n")

    return True if not error else False


def clean_content(content):
    error = False
    if not content.startswith("Rule ID") and not content.startswith("'Rule ID") and not content.startswith("\"Rule ID"):
        if "```" in content:
            content = content.split("```")[1]
            content = re.sub("^\\s*", "", content)
        else:
            error = True
    new_content = ""
    if not error:
        for idx, line in enumerate(content.split("\n")):
            if "'" not in line and "\"" not in line:
                line_split = line.split(",", 2)
                line = ""
                for i in range(len(line_split)):
                    if i != 0:
                        line += ","
                    if line_split[i]:
                        line += "'" + line_split[i] + "'"
            new_content += line + "\n"
        return new_content.replace("\"", "'")
    else:
        return ""


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
        numbering = " \n" + row[0] + ". "
        if int(row[0]) in smells and row[5]:
            prompt += numbering + row[5]
        elif row[7]:
            prompt += numbering + row[7]
        elif int(row[0]) not in smells and not row[7]:
            prompt += numbering + "-"
    return prompt.rstrip()


def task2_trace_requirements(game: Game, model: GPTModel, smells, temperature=0):
    prompt = create_prompt(game, smells)
    output = prompt_in_chatgpt(prompt, model, temperature)
    uid = str(uuid.uuid4())

    no_error = write_output_to_files(uid, prompt, output, smells)
    print("Finished:", uid, smells, no_error)


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


def task2_analyze_tracing(uid: str, csv_gt: str):
    fieldnames = ['Rule ID', 'Is it implemented?', 'Lines of implementation in source code']
    dataset = []

    with (open("../../cases/" + csv_gt + ".csv") as csv_reference, open(
            SUB_DIR + "/csv/csv_" + uid + ".csv") as csv_gpt):
        reader_file1 = list(
            csv.DictReader(csv_reference, fieldnames=fieldnames, delimiter=",", skipinitialspace=True, quotechar="'"))
        reader_file2 = list(
            csv.DictReader(csv_gpt, fieldnames=fieldnames, delimiter=",", skipinitialspace=True, quotechar="'"))
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
                        precision_recall = compare_lines(actual_lines[fieldnames[2]], predicted_lines[fieldnames[2]],
                                                         AMOUNT_LINES_IN_GROUND_TRUTH_CODE)
                        data['Precision'] = precision_recall[0]
                        data['Recall'] = precision_recall[1]

                    dataset.append(data)
    return dataset


if __name__ == '__main__':
    main()
