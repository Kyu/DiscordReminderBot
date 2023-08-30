# Discord Reminder Bot  

Written in Java using JDA, creates reminders for discord users.  


## Setup  
Create `JDA_DISCORD_TOKEN` environment variable using discord token.  
Create `JDA_POSTGRES_PASSWORD` environment variable for the postgres database.

Run 
```
docker run  \
--name reminder-discord-bot  \
-e POSTGRES_PASSWORD=$JDA_POSTGRES_PASSWORD  \
-e POSTGRES_DB="reminder-bot"  \
-p5432:5432  \
-d postgres  
```  

### Demo 

https://www.youtube.com/watch?v=5lu6k1wUxH0

