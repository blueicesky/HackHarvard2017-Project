<img align="right" src="https://github.com/gauravlath07/adivino-backend/blob/master/preview.gif">

# Inspiration
Many people love live sports, but when you watch live sports, you often miss out on the commentaries, most of all you miss out on the information of the players they provide. We wanted to bridge that gap with <b>Machine learning and AI</b>.

# What it does
The word Adivino is Spanish for fortune teller. Adivino is a sports vision app powered by machine learning and AI to provide users a new and innovative experience while watching live sports. When a user does not know the player's information, they can use the camera on their phones to see the player's name in realtime. If the user want to know more details about a player, they can click on the 'More' option to view in depth stats. Using statistics and data we obtained from the Premier League website, we came up with an 'Adivino' score which uses an algorithm we came up with to best rate a player's performance in context and his club's predicted performance and score for future games.

# How we built it
We used <b>Microsoft's Cognitive Services' SDK</b> for Android to accomplish OCR and a custom built realtime text detector to get the information of the player the user is pointing the phone's camera to. We extract the player's name and/or number and then send it to a Python backend hosted on Microsoft Azure for future game predictions and the calculation of the Adivino score. We used Android to make the app that the user will use to interact with our machine learning and text detection model and our backend.

# Challenges we ran into
Handling compatibility issues of libraries in Python.
Getting the text detector working with Microsoft's Cognitive services.
Coming up with the algorithm that we use to describe the player's rating (The Adivino Score).
Coming up with a concise and relevant list of features we want to present to the user.
Setting up Microsoft Azure for backend.
# Accomplishments that we're proud of
We were able to overcome all the issues we had above.
We were able to innovative and make a REALTIME text detector working with Microsoft's Cognitive Services.
# What we learned
We learned to work together as a team and complimenting each other's weaknesses.
We learned to innovate and brainstorm as a team to come up with innovative and efficient solutions.
We learned how to use Microsoft's Cognitive services and setting up a backend using Microsoft Azure. ## What's next for Adivino
We are planning to refine our algorithm to predict the correct score and performance of players.
We are planning to make our realtime detection more accurate and more efficient. (i.e. reduce noise, cleaning up the inputs to the SDK)
We are planning to make our api open source so that way everyone can integrate our API into their apps or websites.


