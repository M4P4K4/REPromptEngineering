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


class Model(Enum):
    GPT_3 = "gpt-3.5-turbo"
    GPT_4 = "gpt-4"


# store your keys in "variables.env"
load_dotenv(dotenv_path=Path("../../../REPromptEngineering/variables.env"))
API_KEY = os.getenv("API_KEY")
ORG_KEY = os.getenv("ORG_KEY")
client = OpenAI(organization=ORG_KEY, api_key=API_KEY)


def main():
    smells = []
    # smells = [8, 9]
    generate_code(smells, Game.DICE, Model.GPT_4, 0)


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
        prompt.rstrip()

    return prompt


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
        prompt.rstrip()

        return prompt

    except HttpError as err:
        print(err)


def generate_code(smells, game, model, temperature):
    output_id = str(uuid.uuid4())
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

    # output_file = open("../../outputs/" + game.value + "/" + "output_" + output_id + ".txt", "x")
    # input_file = open("../../outputs/" + game.value + "/" + "prompt_" + output_id + ".txt", "x")

    # write output
    with open("../../outputs/" + game.value + "/" + "output_" + output_id + ".txt", "x") as output_file:
        for chunk in stream:
            content = chunk.choices[0].delta.content
            if content is not None:
                output_file.write(content)
        output_file.close()

    # write prompt
    with open("../../outputs/" + game.value + "/" + "prompt_" + output_id + ".txt", "x") as input_file:
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
