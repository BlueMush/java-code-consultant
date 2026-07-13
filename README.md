# java-code-consultant

Claude Code 플러그인 마켓플레이스.

## 설치

Claude Code에서:

```
/plugin marketplace add <이 repo의 git URL 또는 로컬 경로>
/plugin install java21-conventions@java-code-consultant
```

로컬 경로로 추가하는 경우:

```
/plugin marketplace add /home/bluemush/work/java-code-consultant
```

## 플러그인 목록

| 플러그인 | 설명 |
|---|---|
| [java21-conventions](plugins/java21-conventions/) | Java 21 코드 작성규칙 — 모던 이디엄, JUnit5+AssertJ 테스트 컨벤션, Spring Boot 3.5 관례, 기능 분해 기준 |

## 개발

플러그인 수정 후 로컬 테스트:

```bash
claude --plugin-dir ./plugins/java21-conventions
```

새 플러그인 추가 시 `plugins/<이름>/` 생성 + `.claude-plugin/marketplace.json`의 `plugins` 배열에 등록.
