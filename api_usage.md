Receiving questions from the game socket:

```json
{
  "action": "ask_question",
  "content": {
    "type": "question",
    "isPoll": true,
    "content": {
      "title": "title",
      "description": "description",
      "options": [
        {
          "title": "title1",
          "description": "description1"
        },
        {
          "title": "title2",
          "description": "description2"
        }
      ]
    }
  }
}
```

Information on the data:

```
Action -> Packet Identifier, use it to know what to do with the data.
Content ->
    Type -> Type of the content, in this case, it's a question.
    IsPoll -> If it's a poll or not, Polls only change the way the results are analyzed. 
    Content -> The actual question data.
        Title -> The title of the question.
        Description -> The description of the question.
        Options -> The options of the question.
            Title -> The title of the option.
            Description -> The description of the option.
```


