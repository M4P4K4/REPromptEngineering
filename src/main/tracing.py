import csv
import os
import re
import uuid
import numpy as np
import matplotlib.pyplot as plot

from openai import OpenAI
from dotenv import load_dotenv
from pathlib import Path
from enum import Enum

from openai.types.chat import ChatCompletion

from read_gsheet import GSheetReader


# store your keys in "variables.env"
load_dotenv(dotenv_path=Path("../../../REPromptEngineering/variables.env"))
API_KEY = os.getenv("API_KEY")
ORG_KEY = os.getenv("ORG_KEY")
client = OpenAI(organization=ORG_KEY, api_key=API_KEY)

# Length of the groundtruth code
LEN_GROUNDTRUTH_CODE = 143


class Game(Enum):
    DICE = "dice"


class GPTModel(Enum):
    GPT_3 = "gpt-3.5-turbo-16k"
    GPT_4 = "gpt-4"


class Smells(Enum):
    # Structure: [[[Smell Variant 1], [Smell Variant 2], ...], "Path to results", "Label for Boxplot"]
    # -> The outer array is iterated through, passing the inner array as a list of all smells for each prompt iteration
    NONE = [[[]], "../../GPT-4 outputs/[RQ1] no smell/", "Smell-free"]
    ONE = [[[8], [9], [10], [11], [14], [15], [16], [17], [18], [19], [20], [21]], "../../GPT-4 outputs/[RQ2] one smell/", "One Smell"]
    ALL = [[[8, 9, 10, 11, 14, 15, 16, 17, 18, 19, 20, 21]], "../../GPT-4 outputs/[RQ2] all smells/", "All Smells"]
    LEXIC = [[[8, 9, 10, 19]], "../../GPT-4 outputs/[RQ3] lexical smells/", "Lexic Smells"]
    SEMANTIC = [[[11, 14, 15, 20]], "../../GPT-4 outputs/[RQ3] semantic smells/", "Semantic Smells"]
    SYNTAX = [[[16, 17, 18, 21]], "../../GPT-4 outputs/[RQ3] syntactic smells/", "Syntax Smells"]


class ReferenceTracing(Enum):
    EXPERT1 = "groundTruthTracing_DiceGame_Expert1"
    EXPERT2 = "groundTruthTracing_DiceGame_Expert2"
    INTERSECTION = "groundTruthTracing_DiceGame_intersection"
    UNIFICATION = "groundTruthTracing_DiceGame_unification"


# list of all available parameters: https://platform.openai.com/docs/api-reference/chat/create
def main():
    #### Step 1: Trace Requirements ####
    tracing = False
    #### Select Settings
    smells = Smells.SYNTAX
    game = Game.DICE
    model = GPTModel.GPT_4
    temperature = 0
    top_p = None
    iterations = 5

    if tracing:
        trace_requirements(smells, game, model, temperature, top_p, iterations)

    #### Step 2: Analyze the tracing & evaluate Performance ####
    analyzing = True
    #### Select Settings
    # reference_tracings = [ReferenceTracing.EXPERT1, ReferenceTracing.EXPERT2, ReferenceTracing.UNIFICATION, ReferenceTracing.INTERSECTION]
    reference_tracings = [ReferenceTracing.UNIFICATION]
    # smells = [Smells.NONE, Smells.ONE, Smells.ALL]
    smells = [Smells.LEXIC, Smells.SEMANTIC, Smells.SYNTAX]
    create_boxplot = True

    if analyzing:
        analyze_tracing(reference_tracings, smells, create_boxplot)
        analyze_tracing_per_requirement(ReferenceTracing.UNIFICATION.value)


def trace_requirements(smells, game, model, temperature, top_p, number_of_iterations):
    all_smells = smells.value[0]
    sub_dir = smells.value[1]
    for smell in all_smells:
        for x in range(number_of_iterations):
            task2_trace_requirements(game, model, smell, sub_dir, temperature, top_p)


def analyze_tracing(reference_tracings, smells, create_boxplot):
    for reference in reference_tracings:
        datasets, labels = [], []
        for s in smells:
            smell = s.value[0]
            sub_dir = s.value[1]
            dataset = build_datasets(reference.value, smell, sub_dir)
            datasets.append(dataset)
            len_dataset = evaluate_performance(dataset)
            labels.append(s.value[2] + "\n n = " + str(len_dataset))
        if create_boxplot:
            build_boxplot(datasets, labels)


def analyze_tracing_per_requirement(csv_name):
    datasets = []

    for s in Smells:
        smells = s.value[0]
        sub_dir = s.value[1]
        datasets.append(build_datasets(csv_name, smells, sub_dir))

    analyze_tp1_per_requirement(datasets)
    analyze_tp2_per_requirement(datasets)


def analyze_tp1_per_requirement(datasets):
    tp1_not_smelly = {}
    tp1_smelly = {}

    for dataset in datasets:
        for data in dataset:
            for d in data:
                rule_id = d["Rule ID"]
                smelly = d["Smelly"]

                implemented = d['Implemented']
                correct_predicted = d['Correct predicted']

                if implemented:
                    if correct_predicted:
                        case = "tp"
                    else:
                        case = "fn"
                else:
                    if correct_predicted:
                        case = "tn"
                    else:
                        case = "fp"
                if smelly:
                    if rule_id in tp1_smelly:
                        tp1_smelly[rule_id].append(case)
                    else:
                        tp1_smelly[rule_id] = [case]
                else:
                    if rule_id in tp1_not_smelly:
                        tp1_not_smelly[rule_id].append(case)
                    else:
                        tp1_not_smelly[rule_id] = [case]

    print("------------------------------------")
    print("TP1 non-smelly")
    for item in tp1_not_smelly.items():
        print_item_tp1(item)
    print("------------------------------------")
    print("TP1 smelly")
    for item in tp1_smelly.items():
        print_item_tp1(item)


def print_item_tp1(item):
    tp = item[1].count("tp")
    tn = item[1].count("tn")
    fp = item[1].count("fp")
    fn = item[1].count("fn")
    print("Requirement", item[0],
          "\ttrue:", tp + tn,
          "\tfalse:", fp + fn,
          "\ttp:", tp,
          "\ttn:", tn,
          "\tfp:", fp,
          "\tfn:", fn
          )


def analyze_tp2_per_requirement(datasets):
    tp2_not_smelly_precision, tp2_not_smelly_recall = {}, {}
    tp2_smelly_precision, tp2_smelly_recall = {}, {}

    for dataset in datasets:
        for data in dataset:
            for d in data:
                rule_id = d["Rule ID"]
                smelly = d["Smelly"]

                ## Evaluating TP2
                if "Precision" in d:
                    if smelly:
                        if rule_id in tp2_smelly_precision:
                            tp2_smelly_precision[rule_id].append(d["Precision"])
                            tp2_smelly_recall[rule_id].append(d["Recall"])
                        else:
                            tp2_smelly_precision[rule_id] = [d["Precision"]]
                            tp2_smelly_recall[rule_id] = [d["Recall"]]
                    else:
                        if d["Rule ID"] in tp2_not_smelly_precision:
                            tp2_not_smelly_precision[rule_id].append(d["Precision"])
                            tp2_not_smelly_recall[rule_id].append(d["Recall"])
                        else:
                            tp2_not_smelly_precision[rule_id] = [d["Precision"]]
                            tp2_not_smelly_recall[rule_id] = [d["Recall"]]

    print("------------------------------------")
    print("TP2 non-smelly PRECISION")
    for item in tp2_not_smelly_precision.items():
        print_item_tp2(item)
    print("------------------------------------")
    print("TP2 smelly PRECISION")
    for item in tp2_smelly_precision.items():
        print_item_tp2(item)
    print("------------------------------------")
    print("TP2 non-smelly RECALL")
    for item in tp2_not_smelly_recall.items():
        print_item_tp2(item)
    print("------------------------------------")
    print("TP2 smelly RECALL")
    for item in tp2_smelly_recall.items():
        print_item_tp2(item)


def print_item_tp2(item):
    print("Requirement", item[0],
          "\tSize:", len(item[1]),
          "\tMin:", round(np.min(item[1]), 2),
          "\tMax:", round(np.max(item[1]), 2),
          "\tAverage:", round(np.average(item[1]), 2),
          "\tMedian:", round(np.median(item[1]), 2)
          )


def build_boxplot(datasets, labels):
    values_precision, values_recall = [], []
    for d in datasets:
        temp_values_prec = []
        for key, value in evaluate_performance_per_ruleid(d, "Precision").items():
            temp_values_prec = [*temp_values_prec, *value]
        values_precision.append(temp_values_prec)

        temp_values_recall = []
        for key, value in evaluate_performance_per_ruleid(d, "Recall").items():
            temp_values_recall = [*temp_values_recall, *value]
        values_recall.append(temp_values_recall)

    nrows, ncols = 2, 1
    if len(datasets) == 1:
        nrows, ncols = 1, 2
    fig, (ax1, ax2) = plot.subplots(nrows, ncols, sharey=True)

    ax1.boxplot(values_precision, labels=labels, meanline=True, showfliers=True, showmeans=True)
    ax2.boxplot(values_recall, labels=labels, meanline=True, showfliers=True, showmeans=True)
    ax1.grid(axis="y", linestyle="--")
    ax2.grid(axis="y", linestyle="--")
    ax1.set_title("Precision")
    ax2.set_title("Recall")

    fig.tight_layout()
    # fig.savefig("../../Tracing Results/Tracing RQ1 & RQ2")
    # fig.savefig("../../Tracing Results/Tracing RQ3")
    plot.show()


def build_datasets(csv_name, smells, sub_dir):
    datasets = []
    print("------------------------------------")
    print(csv_name.split("_")[2].upper(), "\t", smells)  # Name of reference

    with open(sub_dir + "overview.csv") as csv_file:
        fn = ["UID", "Smelly Rules", "Additional Infos"]
        lines = list(csv.DictReader(csv_file, fieldnames=fn, delimiter=",", skipinitialspace=True, quotechar="'"))
        for num, line in enumerate(lines):
            for smell in smells:
                if line[fn[1]] == str(smell) and "error" not in line[fn[2]] and "deleted" not in line[fn[2]]:
                    datasets.append(evaluate_loc_tracing(line[fn[0]], csv_name, smell, sub_dir))
    return datasets


def evaluate_performance(datasets):
    tp, tn, fp, fn = 0, 0, 0, 0
    loc_precision = []
    loc_recall = []
    len_datasets = 0

    for dataset in datasets:
        for data in dataset:
            implemented = data['Implemented']
            correct_predicted = data['Correct predicted']

            if implemented:  # is the requirement implemented in the code?
                if correct_predicted:  # did ChatGPT correctly predict it?
                    tp += 1
                else:
                    fn += 1
                loc_precision.append(data['Precision'])
                loc_recall.append(data['Recall'])
                len_datasets += 1
            else:
                if correct_predicted:
                    tn += 1
                else:
                    fp += 1

    loc_recall = np.mean(loc_recall)
    loc_precision = np.mean(loc_precision)

    precision_recall = [round(x, 2) for x in calculate_precision_recall(tp, tn, fp, fn)]
    print("Impl. Prediction:\t", precision_recall, "\t", (tp + tn + fp + fn), "\t(Precision/Recall/Dataset size)")
    precision_recall = [round(loc_precision, 2), round(loc_recall, 2)]
    print("LOC Prediction:\t\t", precision_recall, "\t", len_datasets, "\t(Precision/Recall/Dataset size)")

    return len_datasets


def evaluate_performance_per_ruleid(datasets, measurement_type):
    performance_per_rule = {}

    for idx, dataset in enumerate(datasets):
        for data in dataset:
            if measurement_type in data:
                key = data['Rule ID']
                if key not in performance_per_rule:
                    performance_per_rule[key] = [data[measurement_type]]
                else:
                    performance_per_rule[key].append(data[measurement_type])

    return performance_per_rule


def write_output_to_files(uid: str, prompt: str, output: ChatCompletion, sub_dir, smells=None):
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
        content = clean_content(output.choices[0].message.content)
        f.write(content)
        f.close()

    error = ""
    if not content:
        error = "error"

    # write overview
    with open(sub_dir + "/overview.csv", "a") as f:
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


def prompt_in_chatgpt(given_prompt, model, temperature, top_p, max_tokens=None):
    return client.chat.completions.create(
        model=model.value,
        temperature=temperature,
        top_p=top_p,
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


def task2_trace_requirements(game: Game, model: GPTModel, smells, sub_dir, temperature=1, top_p=1):
    prompt = create_prompt(game, smells)

    if not temperature:
        temperature = 1
    if not top_p:
        top_p = 1

    output = prompt_in_chatgpt(prompt, model, temperature, top_p)
    uid = str(uuid.uuid4())

    no_error = write_output_to_files(uid, prompt, output, sub_dir, smells)
    print("Finished:", uid, smells, "No error:", no_error)


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
    with open("../../Groundtruth Code/src/groundTruth/DiceGame.java") as codefile:
        idx = 1
        for line in codefile:
            line = line.split("//")  # removes every comment
            code += str(idx) + ". " + line[0]
            if len(line) > 1:
                code += "\n"
            idx += 1
        codefile.close()
    return code


def compare_lines(real_data, prediction, len_all_lines):
    lines_real_data = get_all_lines(real_data)
    lines_prediction = get_all_lines(prediction)

    tp = 0
    tn = len_all_lines - len(lines_prediction)
    fp = len(lines_prediction)
    fn = 0

    for line in lines_real_data:  # LOC in the ground truth
        if line in lines_prediction:  # LOC that GPT-4 predicted
            tp += 1
            fp -= 1
        else:
            fn += 1
            tn -= 1

    return calculate_precision_recall(tp, tn, fp, fn)


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


def get_implementation(line):
    implemented = None
    if line == "Yes":
        implemented = True
    elif line == "No":
        implemented = False
    return implemented


def evaluate_loc_tracing(uid: str, csv_gt: str, smells, sub_dir):
    fn = ['Rule ID', 'Is it implemented?', 'Is it implemented? (smelly)', 'Lines of implementation in source code']
    dataset = []

    with (open("../../Groundtruth Tracings/" + csv_gt + ".csv") as ref_csv, open(sub_dir + "/csv/csv_" + uid + ".csv") as gpt_csv):
        ref_lines = list(csv.DictReader(ref_csv, fieldnames=fn, delimiter=",", skipinitialspace=True, quotechar="'"))
        gpt_lines = list(csv.DictReader(gpt_csv, fieldnames=fn, delimiter=",", skipinitialspace=True, quotechar="'"))
        if len(ref_lines) == len(gpt_lines):
            for idx, ref_line in enumerate(ref_lines):
                if idx != 0:  # Filters header
                    gpt_line = gpt_lines[idx]

                    ref_id = ref_line[fn[0]]
                    smelly = True if int(ref_id) in smells else False
                    ref_impl = get_implementation(ref_line[fn[2 if smelly else 1]])
                    gpt_impl = get_implementation(gpt_line[fn[1]])

                    # if ref_impl is not None and int(ref_id) in [8, 9, 10, 11, 14, 15, 16, 17, 18, 19, 20, 21]:
                    if ref_impl is not None:  # only appends data if requirement is implemented
                        data = {
                            'Rule ID': ref_id,
                            'Implemented': ref_impl,
                            'Correct predicted': True if ref_impl == gpt_impl else False,
                            'Smelly': smelly
                        }
                        if ref_impl and gpt_impl:
                            precision_recall = compare_lines(ref_line[fn[3]], gpt_line[fn[2]], LEN_GROUNDTRUTH_CODE)
                            data['Precision'] = precision_recall[0]
                            data['Recall'] = precision_recall[1]
                        dataset.append(data)
    return dataset


if __name__ == '__main__':
    main()
