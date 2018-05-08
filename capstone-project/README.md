# AND_Capstone
An app to keep track of your current reads and make notes


# Config
This project uses the Goodreads API and will require the Goodreads Developer and Secret key. These maybe acquired from - https://www.goodreads.com/api/keys

In build.gradle file at the app level, a Goodreads Developer and Secret key will need to be added in place of the dummy-strings
        
        it.buildConfigField 'String', 'GOODREADS_DEVELOPER_KEY', "\"--dummy-dev-key--\""
        it.buildConfigField 'String', 'GOODREADS_DEVELOPER_SECRET', "\"-dummy-secret-key--\""


## External Libraries
- java-json: To convert the xml output from the Goodreads API to JSON
- Picasso: To load images 

## Activities
 
#### Main Activity
Displays your current reads along with your notes using a ViewPager

#### Search & Search Results Activity
Used to search for your books and add them to your current reads

#### Archives (Statistics Activity)
Used to store books that have been completed


## Widget
The widget provides a list of your current reads with the number of notes in each 

## Google Play Services
- Google AdMob
- Google Analytics

