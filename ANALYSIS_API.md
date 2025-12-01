# 얼굴 감정 분석 API 명세서

## `POST /api/analysis/face`

얼굴 사진을 받아 감정을 분석하고 결과를 반환합니다. 이 API는 AWS SageMaker 엔드포인트를 호출하여 실제 분석을 수행합니다.

---

### 요청 (Request)

#### Headers

| Key | Value | 설명 |
| :--- | :--- | :--- |
| `Content-Type`| `multipart/form-data` | 이미지 파일을 포함하는 폼 데이터를 전송하기 위해 반드시 필요합니다. |

#### Body

`FormData` 형식으로 이미지 파일을 전송해야 합니다.

| Key | Type | 설명 |
| :--- | :--- | :--- |
| `image` | `File` | **(필수)** 분석할 얼굴 이미지 파일. `image`라는 키 이름을 사용해야 합니다. |

---

### 응답 (Response)

#### ✅ 성공: 200 OK

SageMaker 모델이 반환한 분석 결과를 JSON 형식으로 그대로 반환합니다.

**Content-Type:** `application/json`

```json
// 아래는 SageMaker 모델이 반환하는 응답의 예시이며, 실제 형식은 모델에 따라 다를 수 있습니다.
{
  "prediction": "happy",
  "confidence": 0.98
}
```

#### ❌ 실패: 400 Bad Request

요청이 잘못된 경우 발생합니다. (예: 이미지 파일이 없는 경우)

```json
{
  "error": "Image file is empty"
}
```

#### ❌ 실패: 500 Internal Server Error

서버 내부에서 오류가 발생한 경우입니다.

- **파일 읽기 오류:**
    ```json
    {
      "error": "Error reading image file: [오류 메시지]"
    }
    ```
- **SageMaker 분석 실패 또는 기타 서버 오류:**
    ```json
    {
      "error": "Analysis failed: [오류 메시지]"
    }
    ```

---

### 요청 예시

#### cURL

```bash
curl -X POST \
  http://<YOUR_SPRING_BOOT_SERVER_IP>:8080/api/analysis/face \
  -F 'image=@/path/to/your/image.jpg'
```

#### React Native (JavaScript)

```javascript
// react-native-image-picker 등으로 선택한 이미지 객체
const image = imagePickerResponse.assets[0];

const formData = new FormData();

// 'image' 키로 파일을 첨부합니다.
formData.append('image', {
  uri: image.uri,
  type: image.type, // 예: 'image/jpeg'
  name: image.fileName, // 예: 'photo.jpg'
});

try {
  const response = await fetch('http://<YOUR_SPRING_BOOT_SERVER_IP>:8080/api/analysis/face', {
    method: 'POST',
    body: formData,
    headers: {
      // 'Content-Type': 'multipart/form-data'는 fetch가 자동으로 설정합니다.
    },
  });

  const result = await response.json();
  console.log('분석 결과:', result);

} catch (error) {
  console.error('업로드 및 분석 실패:', error);
}
```
