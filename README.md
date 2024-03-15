# REPromptEngineering
Repository for all the code regarding the research about the influence of 'prompt smells' in prompt engineering

## How to start
- import the following packages:
   - openai
   - python-dotenv
- generate a "variables.env" file directly in the project folder and fill it with the following code:
   ```
   API_KEY=[your_api_key]
   ORG_KEY=[your_organization_key]
   GSHEET_ID=[id_of_gsheet_all_cases]
   ```

## [OPTIONAL] for gsheet usage instead of csv, do this additionally:
- import the following packages:
   - google-api-python-client
   - google-auth-httplib2
   - google-auth-oauthlib
- follow the steps from this gsheet python quickguide: https://developers.google.com/sheets/api/quickstart/python
