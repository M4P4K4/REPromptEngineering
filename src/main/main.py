import csv
import os
import uuid

from openai import OpenAI
from dotenv import load_dotenv
from pathlib import Path
from enum import Enum

from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError


class Game(Enum):
    DICE = "dice"
    ARKANOID = "arkanoid"
    SNAKE = "snake"
    SCOPA = "scopa"
    PONG = "pong"


class GPTModel(Enum):
    GPT_3 = "gpt-3.5-turbo"
    GPT_4 = "gpt-4"


# store your keys in "variables.env"
load_dotenv(dotenv_path=Path("../../../REPromptEngineering/variables.env"))
API_KEY = os.getenv("API_KEY")
ORG_KEY = os.getenv("ORG_KEY")
client = OpenAI(organization=ORG_KEY, api_key=API_KEY)


def main():
    # smells = [8, 9, 10, 11, 12, 13, 14, 15, 16]
    smells = []
    # generate_code(smells, Game.SCOPA, GPTModel.GPT_4, 0)

    # unique_id = int(datetime.now().strftime('%Y%m%d%H%M%S%f'))
    # unique_id = uuid.uuid4()
    unique_id = "4952c293_3342_427d_970c_9b13751e0eb7"
    create_java_code(Game.SCOPA, unique_id)
    # print(unique_id)


# noinspection PyTypeChecker
def create_prompt_with_csv(smells, game):
    prompt = "Create java code for the following description of a game: "

    with open("../../cases/" + game.value + ".csv", encoding="utf8") as csvfile:
        reader = csv.DictReader(csvfile, delimiter=";")
        for row in reader:
            if int(row["Rule ID"]) in smells and row["Smelly Rule"]:
                row_name = "Smelly Rule"
            else:
                row_name = "Non Smelly Option 1"
            prompt += row[row_name] + " "

    return prompt.rstrip()


# source from most of the code: https://github.com/googleworkspace/python-samples/blob/main/sheets/quickstart/quickstart.py
def create_prompt_with_gsheet(smells, game):
    # If modifying these scopes, delete the file token.json.
    scopes = ["https://www.googleapis.com/auth/spreadsheets.readonly"]
    gsheet_id = os.getenv("GSHEET_ID")
    gsheet_range = game.value.title() + "!A2:I"

    creds = None
    # The file token.json stores the user's access and refresh tokens, and is created automatically on the first time.
    if os.path.exists("../../token.json"):
        creds = Credentials.from_authorized_user_file("../../token.json", scopes)

    # If there are no (valid) credentials available, let the user log in.
    if not creds or not creds.valid:
        if creds and creds.expired and creds.refresh_token:
            creds.refresh(Request())
        else:
            flow = InstalledAppFlow.from_client_secrets_file(
                "../../credentials.json", scopes
            )
            creds = flow.run_local_server(port=0)
        # Save the credentials for the next run
        with open("../../token.json", "w") as token:
            token.write(creds.to_json())

    try:
        service = build("sheets", "v4", credentials=creds)

        # Call the Sheets API
        sheet = service.spreadsheets()
        result = (
            sheet.values()
            .get(spreadsheetId=gsheet_id, range=gsheet_range)
            .execute()
        )
        values = result.get("values", [])

        if not values:
            print("No data found.")
            return

        # create the prompt from results
        prompt = "Create java code for the following description of a game: "
        for row in values:
            if int(row[0]) in smells and row[5]:
                row_id = 5
                prompt += row[row_id] + " "
            elif row[6]:
                row_id = 6
                prompt += row[row_id] + " "

        return prompt.rstrip()

    except HttpError as err:
        print(err)


def generate_code(smells: list[int], game: Game, model: GPTModel, temperature: int):
    unique_id = str(uuid.uuid4()).replace("-", "_")
    # unique_id = int(datetime.now().strftime('%Y%m%d%H%M%S%f'))

    if not smells:
        smelly = False
    else:
        smelly = True

    # given_prompt = create_prompt_with_csv(smells, game)
    given_prompt = create_prompt_with_gsheet(smells, game)

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

    # write output
    with open("../../outputs/" + game.value + "/output/output_" + str(unique_id) + ".txt", "x") as output_file:
        for chunk in stream:
            content = chunk.choices[0].delta.content
            if content is not None:
                output_file.write(content)
        output_file.close()

    # write prompt
    with open("../../outputs/" + game.value + "/prompt/prompt_" + str(unique_id) + ".txt", "x") as input_file:
        input_file.write(given_prompt)
        input_file.close()

    write_overview_file(game, smells, smelly, model, temperature, unique_id)
    create_java_code(game, unique_id)


def write_overview_file(game, smells, smelly, model, temperature, unique_id):
    overview_filepath = "../../outputs/" + game.value + "/overview_" + game.value + ".csv"
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

        writer.writerow({"Ground Truth": (not smelly),
                         "Smelly Rules": smells_string,
                         "Model": model.value,
                         "Temperature": temperature,
                         "UUID": unique_id})


def create_java_code(game: Game, unique_id: uuid, old_uuid=None):
    if not old_uuid:
        old_uuid = unique_id
    unique_id = unique_id.replace("-", "_")

    if game == Game.DICE:
        old_name = "DiceGame"
        new_name = "DiceGame_" + str(unique_id)
    elif game == Game.SCOPA:
        old_name = "Main"
        new_name = "Scopa_" + str(unique_id)
    else:
        old_name = "TEST"
        new_name = "TEST_"

    code = "package generatedCode." + game.value + ";\n\n"

    # read code
    with open("../../outputs/" + game.value + "/output/output_" + str(old_uuid) + ".txt") as f:
        output = f.readlines()
        is_code = False
        for line in output:
            if line.startswith("```") and not is_code:
                is_code = True
            elif line.startswith("```") and is_code:
                break
            elif is_code:
                code += line.replace(old_name, new_name)
        f.close()

    # write code
    with open("../../src/Code Output/src/generatedCode/" + game.value + "/" + new_name + ".java", "x") as f:
        f.write(code)
        f.close()


if __name__ == '__main__':
    main()
