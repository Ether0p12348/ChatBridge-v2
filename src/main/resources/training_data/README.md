# Training Data

TODO: fix training data. Currently the src.lang and tgt.lang are not in the target language but in English.

---

## Requests

Uses OpenAI Prompts. Variables are in the same format.

### Messages
```json
{
  "tgt": "en-US",
  "msg": "皆さん、ちょっと聞いて！**重要**: サーバー設定を変更しました。詳しくは [ここ](https://example.jp/docs) を見てね。正直、この遅延はマジでクソだけど、対処法はあるから心配しないで。<@&999> ||予定は非公開||"
}
```

| Variable | Description              |
|----------|--------------------------|
| `tgt`    | The target locale        |
| `msg`    | The message to translate |

### Embeds
```json
{
  "tgt": "en-US",
  "msg": "Всем привет! Ниже — краткое резюме обновления.",
  "title": "Обновление платформы: ускорен ответ бота",
  "author": "Команда ChatBridge",
  "desc": "Мы внедрили улучшения очереди и кеширования. В результате время ответа сократилось, а стабильность возросла. Подробности и инструкции по откату — по ссылке ниже.",
  "footer": "ID запроса: 4F8A-21C · Свяжитесь с поддержкой при вопросах",
  "fields": "[{\"name\":\"Что изменилось\",\"value\":\"• Оптимизирована очередь сообщений\n• Улучшена обработка ошибок\n• Снижена нагрузка на API\"},{\"name\":\"Кого касается\",\"value\":\"Все серверы; минимальная версия клиента 2.3+\"},{\"name\":\"Обходное решение\",\"value\":\"Если бот отвечает медленно, используйте команду `/status` или проверьте страницу статуса.\"},{\"name\":\"Ссылки\",\"value\":\"Документация: https://example.com/docs\nСтатус: https://status.example.com\"}]"
}
```
| Variable      | Description                                               |
|---------------|-----------------------------------------------------------|
| `tgt`         | The target locale                                         |
| `msg`         | The message outside of the embed                          |
| `title`       | The embed's title                                         |
| `author`      | The embed's author (names shouldn't be translated)        |
| `description` | The embed's body                                          |
| `footer`      | The embed's footer                                        |
| `fields`      | List of fields inside the embed; _JSON array as a string_ |

#### Fields
| Variable | Description            |
|----------|------------------------|
| `name`   | The name of the field  |
| `value`  | The value of the field |

---

## Responses

Responds JSON as a string.

### Messages

```json
{
  "src": {
    "tag": "ja",
    "lang": "Japanese"
  },
  "tgt": {
    "tag": "en-US",
    "lang": "English",
    "e": "Everyone, quick update! **Important**: We changed the server settings. Check [here](https://example.jp/docs) for details. Honestly, this delay is freaking shit, but there’s a workaround, so don’t worry. <@&999> ||The schedule is private||",
    "s": "Everyone, quick update! **Important**: We changed the server settings. Check [here](https://example.jp/docs) for details. Honestly, this delay is *really annoying*, but there’s a workaround, so don’t worry. <@&999> ||The schedule is private||"
  }
}
```

| Variable | Description                |
|----------|----------------------------|
| `src`    | Source data                |
| `tgt`    | Target data & translations |

#### Source (src)

| Variable | Description                                 |
|----------|---------------------------------------------|
| `tag`    | The source locale                           |
| `lang`   | The source locale's name in target language |

#### Target (tgt)

| Variable | Description                                                                         |
|----------|-------------------------------------------------------------------------------------|
| `tag`    | The target locale                                                                   |
| `lang`   | The target locale's name in target language                                         |
| `e`      | The _explicit_ translated message - includes explicit language                      |
| `s`      | The _safe_ translated message - replaces explicit language (replaced is italicized) |

### Embeds

```json
{
  "src": {
    "tag": "ru",
    "lang": "Russian"
  },
  "tgt": {
    "tag": "en-US",
    "lang": "English",
    "e": {
      "message": "Hi everyone! Here’s a quick summary of the update.",
      "title": "Platform Update: Faster Bot Responses",
      "author": "ChatBridge Team",
      "description": "We rolled out queue and caching improvements. As a result, response times are lower and stability is higher. See the links below for details and rollback steps.",
      "footer": "Request ID: 4F8A-21C · Contact support if you have questions",
      "fields": [
        {
          "name": "What changed",
          "value": "• Optimized message queue\n• Improved error handling\n• Reduced API load"
        },
        {
          "name": "Who’s affected",
          "value": "All servers; minimum client version 2.3+"
        },
        {
          "name": "Workaround",
          "value": "If the bot is slow to reply, use `/status` or check the status page."
        },
        {
          "name": "Links",
          "value": "Docs: https://example.com/docs\nStatus: https://status.example.com"
        }
      ]
    },
    "s": {
      "message": "Hi everyone! Here’s a quick summary of the update.",
      "title": "Platform Update: Faster Bot Responses",
      "author": "ChatBridge Team",
      "description": "We rolled out queue and caching improvements. As a result, response times are lower and stability is higher. See the links below for details and rollback steps.",
      "footer": "Request ID: 4F8A-21C · Contact support if you have questions",
      "fields": [
        {
          "name": "What changed",
          "value": "• Optimized message queue\n• Improved error handling\n• Reduced API load"
        },
        {
          "name": "Who’s affected",
          "value": "All servers; minimum client version 2.3+"
        },
        {
          "name": "Workaround",
          "value": "If the bot is slow to reply, use `/status` or check the status page."
        },
        {
          "name": "Links",
          "value": "Docs: https://example.com/docs\nStatus: https://status.example.com"
        }
      ]
    }
  }
}
```

| Variable | Description                |
|----------|----------------------------|
| `src`    | Source data                |
| `tgt`    | Target data & translations |

#### Source (src)

| Variable | Description                                 |
|----------|---------------------------------------------|
| `tag`    | The source locale                           |
| `lang`   | The source locale's name in target language |

#### Target (tgt)

| Variable | Description                                                                       |
|----------|-----------------------------------------------------------------------------------|
| `tag`    | The target locale                                                                 |
| `lang`   | The target locale's name in target language                                       |
| `e`      | The _explicit_ translated embed - includes explicit language                      |
| `s`      | The _safe_ translated embed - replaces explicit language (replaced is italicized) |

#### Translations (tgt.e, tgt.s)

| Variable      | Description                                        |
|---------------|----------------------------------------------------|
| `tgt`         | The target locale                                  |
| `msg`         | The message outside of the embed                   |
| `title`       | The embed's title                                  |
| `author`      | The embed's author (names shouldn't be translated) |
| `description` | The embed's body                                   |
| `footer`      | The embed's footer                                 |
| `fields`      | List of fields inside the embed                    |

#### Fields (tgt.e.fields, tgt.s.fields)

| Variable | Description            |
|----------|------------------------|
| `name`   | The name of the field  |
| `value`  | The value of the field |

---

## Schemas

Defined in OpenAI Prompts

### Messages

```json
{
  "name": "cb_message_response",
  "strict": false,
  "schema": {
    "type": "object",
    "additionalProperties": false,
    "required": ["src", "tgt"],
    "properties": {
      "src": {
        "type": "object",
        "additionalProperties": false,
        "required": ["tag", "lang"],
        "properties": {
          "tag": {
            "type": "string"
          },
          "lang": {
            "type": "string"
          }
        }
      },
      "tgt": {
        "type": "object",
        "additionalProperties": false,
        "required": ["tag", "lang", "e", "s"],
        "properties": {
          "tag": {
            "type": "string"
          },
          "lang": {
            "type": "string"
          },
          "e": {
            "type": "string"
          },
          "s": {
            "type": "string"
          }
        }
      }
    }
  }
}
```

### Embeds

```json
{
  "name": "cb_embed_response",
  "strict": false,
  "schema": {
    "type": "object",
    "additionalProperties": false,
    "required": ["src", "tgt"],
    "properties": {
      "src": {
        "type": "object",
        "additionalProperties": false,
        "required": ["tag", "lang"],
        "properties": {
          "tag": {
            "type": "string"
          },
          "lang": {
            "type": "string"
          }
        }
      },
      "tgt": {
        "type": "object",
        "additionalProperties": false,
        "required": ["tag", "lang", "e", "s"],
        "properties": {
          "tag": {
            "type": "string"
          },
          "lang": {
            "type": "string"
          },
          "e": {
            "type": "object",
            "additionalProperties": false, 
            "required": ["msg", "title", "author", "desc", "footer", "fields"],
            "properties": {
              "msg": {
                "type": ["string", "null"]
              },
              "title": {
                "type": ["string", "null"],
                "maxLength": 256
              },
              "author": {
                "type": ["string", "null"],
                "maxLength": 256
              },
              "desc": {
                "type": ["string", "null"],
                "maxLength": 4096
              },
              "footer": {
                "type": ["string", "null"],
                "maxLength": 2048
              },
              "fields": {
                "type": ["array", "null"],
                "maxItems": 25,
                "items": {
                  "type": "object",
                  "additionalProperties": false,
                  "required": ["name", "value"],
                  "properties": {
                    "name": {
                      "type": "string",
                      "minLength": 1,
                      "maxLength": 256
                    },
                    "value": {
                      "type": "string",
                      "minLength": 1,
                      "maxLength": 1024
                    }
                  }
                }
              }
            }
          },
          "s": {
            "type": "object",
            "additionalProperties": false,
            "required": ["msg", "title", "author", "desc", "footer", "fields"],
            "properties": {
              "msg": {
                "type": ["string", "null"]
              },
              "title": {
                "type": ["string", "null"],
                "maxLength": 256
              },
              "author": {
                "type": ["string", "null"],
                "maxLength": 256
              },
              "desc": {
                "type": ["string", "null"],
                "maxLength": 4096
              },
              "footer": {
                "type": ["string", "null"],
                "maxLength": 2048
              },
              "fields": {
                "type": ["array", "null"],
                "maxItems": 25,
                "items": {
                  "type": "object",
                  "additionalProperties": false,
                  "required": ["name", "value"],
                  "properties": {
                    "name": {
                      "type": "string",
                      "minLength": 1,
                      "maxLength": 256
                    },
                    "value": {
                      "type": "string",
                      "minLength": 1,
                      "maxLength": 1024
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
```