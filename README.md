# java-code-consultant

Claude Code 플러그인 마켓플레이스.

## 설치

Claude Code에서:

```
/plugin marketplace add BlueMush/java-code-consultant
/plugin install java-code-consultant@java-code-consultant
```

(로컬 체크아웃을 쓰는 경우 `add` 뒤에 repo 경로를 넣어도 된다:
`/plugin marketplace add /home/bluemush/work/java-code-consultant`)

## 플러그인 목록

| 플러그인 | 설명 |
|---|---|
| [java-code-consultant](plugins/java-code-consultant/) | Java 21 코드 작성규칙 — 모던 이디엄, JUnit5+AssertJ 테스트 컨벤션, Spring Boot 3.5 관례, 기능 분해 기준 + `/java-code-consultant:review` 리뷰 커맨드 |

## 개발

플러그인 수정 후 로컬 테스트:

```bash
claude --plugin-dir ./plugins/java-code-consultant
```

스킬이 실제로 발동해 심어둔 위반을 잡는지 자동 확인(1~3분 소요):

```bash
tests/smoke.sh
```

기대 결과 목록은 [tests/EXPECTED.md](tests/EXPECTED.md).

새 플러그인 추가 시 `plugins/<이름>/` 생성 + `.claude-plugin/marketplace.json`의 `plugins` 배열에 등록.
규칙 변경 시 `plugin.json`의 `version`을 올리고 [CHANGELOG.md](CHANGELOG.md)에 기록한다.
