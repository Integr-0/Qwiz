Receiving questions from the game socket:

```json
{
  "action": "ask_question",
  "content": {
    "type": "select",
    "isPoll": true,
    "data": {
      "title": "title",
      "description": "description",
      "options": [
        {
          "title": "title1",
          "hint": "description1"
        },
        {
          "title": "title2",
          "hint": "description2"
        }
      ]
    }
  }
}
```

```json
{
  "action": "ask_question",
  "content": {
    "type": "number_select",
    "isPoll": false,
    "data": {
      "title": "title",
      "description": "description",
      "min": 10,
      "max": 20
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
    Data -> The actual question data.
        Title -> The title of the question.
        Description -> The description of the question.
        Options -> The options of the question.
            Title -> The title of the option.
            Hint -> The description of the option.
```


