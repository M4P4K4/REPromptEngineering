import os

from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError


# source from most of the code: https://github.com/googleworkspace/python-samples/blob/main/sheets/quickstart/quickstart.py
class GSheetReader:
    def __init__(self, game):
        self.game = game
        self.values = self.get_values()

    def get_values(self):
        game = self.game
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

            if values:
                return values
            else:
                print("No data found.")
                return

        except HttpError as err:
            print(err)
