# Approach & Technical Notes

## 1. What We’re Building
A small Spring Boot microservice that stores a list of **sensitive words/phrases** (CRUD) and exposes a **sanitize** endpoint that returns an input string with those items masked using asterisks.  
Example: incoming chat message → API → returns the same text with `SELECT`, `ORDER BY`, `*`, etc. replaced with masked values. `SELECT * FROM table1 ORDER BY column;` -> `****** * FROM table1 ******** column;`

---

## 2. Requirements Recap
- RESTful API in Java + Spring Boot.
- Swagger/OpenAPI docs for all endpoints.
- MSSQL database for persistence (JPA/Hibernate).
- Unit tests.
- A production-ready deployment outline.
- Handle both **single words** and **phrases/symbols** safely when sanitizing.

---

## 3. Data Model
`SensitiveWord { id: Long, word: String }`

- `word` is **unique** (case-insensitive semantics implemented in service class).
- **Normalize** to lowercase so that we can ensure to keep comparisons consistent.

---

## 4. API Design
Base path: `/api/sensitive-words`

| Method | Path | Description |
|--------|------|-------------|
| `GET`  | `/`  | List all words (alphabetical, case-insensitive). |
| `POST` | `/`  | Create a word or phrase (JSON: `{ "word": "SELECT" }`). |
| `PUT`  | `/{id}` | Update a stored word/phrase based on the given ID. |
| `DELETE` | `/{id}` | Delete a stored word/phrase based on the given ID. |
| `POST` | `/sanitize` | Sanitize the incoming text (JSON: `{ "text": "Select * from users order by name" }`). |

**Reponses of request are all structured as below:**
```json
{
  "timestamp": "...",
  "code": 200,
  "message": "Action successful",
  "data": ...
}
```

---

## 5. Sanitization – Problem & Solution
### Problem <br>
The Problem that I encountered was that when I mask words by merely replacing them I would run into the following issue: <br>
When my masking words are: `SELECT`, `ORDER BY`, and `DELETE`. But the tex to sanitize is for example: `Select * from selectedTable order by name;` <br> 
The select in selected (`select`ed) would also get sanitized. (`****** * from ******edTable ******** name;`)

Mask substrings inside bigger words.
e.g., masking prep → "prepare" becomes "****are".

### Solution <br>
These patterns:
- Are case-insensitive (`?i`)
- Use **lookbehind** `(?<=\W|^)` to match when the word starts at the beginning of a string or after a non-word character
- Use **lookahead** `(?=\W|$)` to match when the word ends before a non-word character or the end of the string

<img width="331" height="265" alt="image" src="https://github.com/user-attachments/assets/a66c741a-1ff6-45a2-9900-e866d5f30b73" />
<img width="336" height="249" alt="image" src="https://github.com/user-attachments/assets/4ff822f7-5333-4506-9b56-634580b6b76c" />

On the above was the issue that I kept on running into, below is an explanation of what how I addressed the issue.

So I added lookarounds to solve the issue:
<img width="1273" height="224" alt="image" src="https://github.com/user-attachments/assets/bbbd2341-1fff-4286-bae1-2d24408cba4b" />
<img width="794" height="215" alt="image" src="https://github.com/user-attachments/assets/54586161-71ca-4ba7-987d-7596165390a2" />

I generated a regex per term/word using WordUtils.buildBoundaryRegex(term):

```regex
(?i)(?<=\W|^)select(?=\W|$)
(?i)(?<=\W|^)order\ by(?=\W|$)
(?i)(?<=\W|^)\*(?=\W|$)
```

## Regex Matching Logic

**Plain words → word boundaries:**
```regex
(?i)\bSELECT\b
```

**Phrases/symbols → look-arounds:**
```regex
(?i)(?<=\W|^)ORDER\ BY(?=\W|$)
```

I made use of `Pattern.quote()` to treat special characters literally when building the regex.

### Below is some of the regex paterns used:
```regex
"SELECT"   →  (?i)\bSELECT\b
"ORDER BY" →  (?i)(?<=\W|^)ORDER\ BY(?=\W|$)
"*"        →  (?i)(?<=\W|^)\*(?=\W|$)
```

---

## 6. Resources Used:

Below is some of the resources taht I used developing: 

- [Regex lookahead, lookbehind, and atomic groups — Stack Overflow](https://stackoverflow.com/questions/2973436/regex-lookahead-lookbehind-and-atomic-groups) - Clear Q&A on lookaround concepts.
- [Regexr.com](https://regexr.com/) - Interactive regex testing and visualization tool.
- [Mastering Lookahead and Lookbehind — RexEgg](https://www.rexegg.com/regex-lookarounds.html) - Deep dive into lookarounds, zero-width assertions, and advanced regex behavior.
- [Pattern quote(String) method in Java with Examples] (https://www.geeksforgeeks.org/java/pattern-quotestring-method-in-java-with-examples/) - `Patern.Quote()`
- [Swagger — Getting Started](https://swagger.io/docs/specification/about/) — Official overview of the OpenAPI Specification and its purpose.
- [Spring Boot + Swagger 3 Example (Baeldung)](https://www.baeldung.com/spring-rest-openapi-documentation) — Guide on adding Swagger/OpenAPI to Spring Boot projects using springdoc-openapi.
- [Spring Boot REST API Documentation with Swagger](https://www.javainuse.com/spring/boot_swagger3) — Step-by-step tutorial for integrating Swagger 3 with annotations.

