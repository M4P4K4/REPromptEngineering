import csv
import os
import uuid
from openai import OpenAI
from dotenv import load_dotenv
from pathlib import Path
from enum import Enum


class Game(Enum):
    DICE = "dice"
    ARKANOID = "arkanoid"
    SNAKE = "snake"
    SCOPA = "scopa"
    PONG = "pong"


class Model(Enum):
    GPT_3 = "gpt-3.5-turbo"
    GPT_4 = "gpt-4"


# store your keys in "variables.env"
load_dotenv(dotenv_path=Path("../../../REPromptEngineering/variables.env"))
API_KEY = os.getenv("API_KEY")
ORG_KEY = os.getenv("ORG_KEY")
client = OpenAI(organization=ORG_KEY, api_key=API_KEY)


def main():
    # Array with all smells we want to implement
    # smells = [1, 3]
    smells = [8, 9]
    generate_code(smells, Game.DICE, Model.GPT_4, 0)


# noinspection PyTypeChecker
def create_prompt(smells, game):
    prompt = "Create java code for the following description of a game: "

    with open("../../cases/" + game.value + ".csv", encoding="utf8") as csvfile:
        reader = csv.DictReader(csvfile, delimiter=";")
        for row in reader:
            if int(row["Rule ID"]) in smells and row["Smelly Rule"]:
                row_name = "Smelly Rule"
            else:
                row_name = "Non Smelly Option 1"
            prompt += row[row_name] + " "
        prompt.rstrip()

    return prompt


def generate_code(smells, game, model, temperature):
    output_id = str(uuid.uuid4())
    if not smells:
        smelly = False
    else:
        smelly = True

    given_prompt = create_prompt(smells, game)

    # send input
    stream = client.chat.completions.create(
        model=model.value,
        temperature=temperature,
        messages=[{
            "role": "user",
            "content": given_prompt
        }],
        stream=True,
    )

    output_file = open("../../outputs/" + game.value + "/" + "output_" + output_id + ".txt", "x")
    input_file = open("../../outputs/" + game.value + "/" + "prompt_" + output_id + ".txt", "x")

    # write output
    for chunk in stream:
        content = chunk.choices[0].delta.content
        if content is not None:
            output_file.write(content)
    output_file.close()

    # write prompt
    input_file.write(given_prompt)
    input_file.close()

    # write overview csv
    with open("../../outputs/overview_" + game.value + ".csv", "a", encoding="utf8", newline='') as csvfile:
        fieldnames = ["Ground Truth", "Smelly Rules", "UUID"]
        writer = csv.DictWriter(csvfile, delimiter=";", fieldnames=fieldnames)
        smells_string = ""

        for idx, smell in enumerate(smells):
            smells_string += str(smell)
            if len(smells) - 1 > idx:
                smells_string += ", "

        # writer.writeheader()
        writer.writerow({"Ground Truth": (not smelly), "Smelly Rules": smells_string, "UUID": output_id})


if __name__ == '__main__':
    main()
