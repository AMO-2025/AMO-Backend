# Game API Documentation

This document provides the specification for the Game API endpoints.

## Authentication

All endpoints require a `Bearer` token in the `Authorization` header.

`Authorization: Bearer <your-jwt-token>`

---

## 1. Play Emotion Choice Game

- **Endpoint:** `POST /api/game/play/emotion-choice`
- **Description:** Submits the result of an emotion choice game session.

### Request

#### Headers

| Key           | Value                   |
|---------------|-------------------------|
| `Authorization` | `Bearer <jwt-token>`    |
| `Content-Type`  | `application/json`      |

#### Body

| Field         | Type    | Description                                     |
|---------------|---------|-------------------------------------------------|
| `npcID`       | String  | The ID of the NPC for the game session.         |
| `mapID`       | String  | The ID of the map where the game took place.    |
| `targetEmotion` | String  | The target emotion for the game.                |
| `userEmotion` | String  | The emotion selected by the user.               |
| `isCorrect`   | Boolean | Whether the user's choice was correct.          |

**Example:**

```json
{
  "npcID": "npc_1",
  "mapID": "map_1",
  "targetEmotion": "HAPPY",
  "userEmotion": "HAPPY",
  "isCorrect": true
}
```

### Response

#### Success (200 OK)

| Field         | Type    | Description                                     |
|---------------|---------|-------------------------------------------------|
| `success`     | Boolean | `true` if the session was saved successfully.   |
| `message`     | String  | A success message.                              |
| `isCorrect`   | Boolean | Whether the user's choice was correct.          |
| `targetEmotion` | String  | The target emotion for the game.                |
| `userEmotion` | String  | The emotion selected by the user.               |

**Example:**

```json
{
  "success": true,
  "message": "감정 선택 게임이 완료되었습니다.",
  "isCorrect": true,
  "targetEmotion": "HAPPY",
  "userEmotion": "HAPPY"
}
```

#### Error (400 Bad Request)

| Field     | Type    | Description                                     |
|-----------|---------|-------------------------------------------------|
| `success` | Boolean | `false` if the request failed.                  |
| `message` | String  | An error message.                               |

**Example:**

```json
{
  "success": false,
  "message": "회원을 찾을 수 없습니다."
}
```

---

## 2. Play Face Photo Game

- **Endpoint:** `POST /api/game/play/face-photo`
- **Description:** Submits the result of a face photo game session, including the captured image.

### Request

#### Headers

| Key           | Value                   |
|---------------|-------------------------|
| `Authorization` | `Bearer <jwt-token>`    |
| `Content-Type`  | `application/json`      |

#### Body

| Field         | Type    | Description                                     |
|---------------|---------|-------------------------------------------------|
| `npcID`       | String  | The ID of the NPC for the game session.         |
| `mapID`       | String  | The ID of the map where the game took place.    |
| `targetEmotion` | String  | The target emotion for the game.                |
| `userEmotion` | String  | The emotion recognized from the user's photo.   |
| `isCorrect`   | Boolean | Whether the recognized emotion was correct.     |
| `imageBase64` | String  | The user's photo, encoded in Base64.            |
| `confidence`  | Double  | The confidence score of the emotion recognition.|

**Example:**

```json
{
  "npcID": "npc_2",
  "mapID": "map_1",
  "targetEmotion": "SAD",
  "userEmotion": "SAD",
  "isCorrect": true,
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
  "confidence": 0.987
}
```

### Response

#### Success (200 OK)

| Field         | Type    | Description                                     |
|---------------|---------|-------------------------------------------------|
| `success`     | Boolean | `true` if the session was saved successfully.   |
| `message`     | String  | A success message.                              |
| `isCorrect`   | Boolean | Whether the recognized emotion was correct.     |
| `targetEmotion` | String  | The target emotion for the game.                |
| `userEmotion` | String  | The emotion recognized from the user's photo.   |
| `s3ImageKey`  | String  | The key for the uploaded image in Amazon S3.    |
| `confidence`  | Double  | The confidence score of the emotion recognition.|

**Example:**

```json
{
  "success": true,
  "message": "얼굴 촬영 게임이 완료되었습니다.",
  "isCorrect": true,
  "targetEmotion": "SAD",
  "userEmotion": "SAD",
  "s3ImageKey": "game-images/1/map_1/npc_2/correct/20251031_103000.jpg",
  "confidence": 0.987
}
```

#### Error (400 Bad Request)

| Field     | Type    | Description                                     |
|-----------|---------|-------------------------------------------------|
| `success` | Boolean | `false` if the request failed.                  |
| `message` | String  | An error message.                               |

**Example:**

```json
{
  "success": false,
  "message": "잘못된 이미지 형식입니다. Base64 인코딩을 확인해주세요."
}
```

---

## 3. Get Game Statistics

- **Endpoint:** `GET /api/game/statistics`
- **Description:** Retrieves detailed game statistics for the authenticated user, categorized by game type and emotion.

### Request

#### Headers

| Key           | Value                   |
|---------------|-------------------------|
| `Authorization` | `Bearer <jwt-token>`    |

### Response

#### Success (200 OK)

The response is a JSON object containing statistics for `facePhotoGame` and `emotionChoiceGame`.

| Field             | Type   | Description                                     |
|-------------------|--------|-------------------------------------------------|
| `facePhotoGame`   | Object | Statistics for the Face Photo game.             |
| `emotionChoiceGame` | Object | Statistics for the Emotion Choice game.         |

Each game type object has the following structure:

| Field             | Type          | Description                                     |
|-------------------|---------------|-------------------------------------------------|
| `totalGames`      | Long          | The total number of games played.               |
| `correctAnswers`  | Long          | The total number of correct answers.            |
| `overallAccuracy` | Double        | The overall accuracy in percent.                |
| `emotionStatistics` | Map[String, Object] | A map of statistics for each emotion.         |

Each emotion statistic object has the following structure:

| Field      | Type   | Description                                     |
|------------|--------|-------------------------------------------------|
| `correct`  | Long   | The number of correct answers for the emotion.  |
| `total`    | Long   | The total number of questions for the emotion.  |
| `accuracy` | Double | The accuracy for the emotion in percent.        |

**Example:**

```json
{
  "facePhotoGame": {
    "totalGames": 10,
    "correctAnswers": 7,
    "overallAccuracy": 70.0,
    "emotionStatistics": {
      "happy": { "correct": 3, "total": 4, "accuracy": 75.0 },
      "sad": { "correct": 2, "total": 3, "accuracy": 66.67 },
      "angry": { "correct": 2, "total": 3, "accuracy": 66.67 },
      "surprised": { "correct": 0, "total": 0, "accuracy": 0.0 },
      "fear": { "correct": 0, "total": 0, "accuracy": 0.0 },
      "disgust": { "correct": 0, "total": 0, "accuracy": 0.0 },
      "neutral": { "correct": 0, "total": 0, "accuracy": 0.0 }
    }
  },
  "emotionChoiceGame": {
    "totalGames": 15,
    "correctAnswers": 12,
    "overallAccuracy": 80.0,
    "emotionStatistics": {
      "happy": { "correct": 5, "total": 6, "accuracy": 83.33 },
      "sad": { "correct": 4, "total": 5, "accuracy": 80.0 },
      "angry": { "correct": 3, "total": 4, "accuracy": 75.0 },
      "surprised": { "correct": 0, "total": 0, "accuracy": 0.0 },
      "fear": { "correct": 0, "total": 0, "accuracy": 0.0 },
      "disgust": { "correct": 0, "total": 0, "accuracy": 0.0 },
      "neutral": { "correct": 0, "total": 0, "accuracy": 0.0 }
    }
  }
}
```

#### Error (404 Not Found)

Returns an empty body if the user is not found.


---

## 6. Health Check

- **Endpoint:** `GET /api/game/health`
- **Description:** Checks if the game service is running.

### Request

No headers or body required.

### Response

#### Success (200 OK)

A plain text response.

**Example:**

```
Game service is running
```
