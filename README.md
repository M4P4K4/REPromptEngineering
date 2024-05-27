# REPromptEngineering
Repository for all the code regarding the research about the influence of 'prompt smells' in prompt engineering.

## Overview
Here are the most relevant files and paths:
- ***/GPT-4 outputs/***: All prompts and outputs we used in our study. For each research question and their scenarios, we clustered everything in the respective subfolders. Structure for the subfolders:
  - ***/csv/***: All outputs from ChatGPT processed in a standardized CSV format
  - ***/output/***: All outputs from ChatGPT, including all metadata
  - ***/prompt/***: All prompts we used
  - ***/overview.csv***: lists all prompt/output iterations with their respective UUID and the selected smells
- ***/Groundtruth Code/src/groundtruth/DiceGame.java***: Developed groundtruth code, based on our requirements.
- ***/Groundtruth Tracings/***: All expert tracings, including the unified and intersected versions.
- ***/src/main/tracing.py***: The code we used to do the automated prompting and analyzing (see Header "Features").
- ***/Tracing Results/***: All results for the tracing performance, including generated boxplots

## Dependencies
we use the following python packages:
  - openai
  - python-dotenv
  - matplotlib
  - numpy

make sure to install them if you want to use our programm.

## Features
### Step 1: Trace Requirements
If you want to use our program for prompting with ChatGPT, you must do the following:

- generate a "variables.env" file directly in the project folder and fill it with the following code:
   ```
   API_KEY=[your_api_key]
   ORG_KEY=[your_organization_key]
   GSHEET_ID=[id_of_your_gsheet_with_all_requirements]
   ```
- import the following packages:
   - google-api-python-client
   - google-auth-httplib2
   - google-auth-oauthlib
- follow the steps from this gsheet python quickguide: https://developers.google.com/sheets/api/quickstart/python

In traying.py, you can adjust all relevant settings in the main function for Step 1:
- tracing: Boolean, select if you want to do the tracing or skip it
- smells: Smells (Enum), smells that you want to prompt
- game: Game (Enum), the Study Object you want to use for the prompt
- model: Model (Enum), select the ChatGPT model you want to use
- temperature: int, temperature for the ChatGPT prompt. Between 0 and 1
- top_p: top_p int, setting for ChatGPT. Between 0 and 1
- iterations: int, number of iterations for each prompt

### Step 2: Analyze the tracing & evaluate Performance

To analyze and evaluate all results from the ***/GPT-4 outputs/*** folder. You can adjust the following settings:
- analyzing: Boolean, select if you want to do the analysis or skip it
- reference_tracings = list, select which References you want to consider the groundtruth (ReferenceTracing Enum)
- smells: list of Smells (Enum), all smells that you want to analyze
- create_boxplot = Boolean, choose if you want to create a boxplot