---
description: 변경된(또는 지정한) 자바 파일을 java-code-consultant 규칙으로 리뷰
argument-hint: "[파일·디렉터리 경로 — 생략 시 git 변경분]"
---

java-code-consultant 스킬의 규칙으로 자바 코드를 리뷰하라.

1. **대상 결정**:
   - `$ARGUMENTS`가 **GitHub PR URL 또는 번호**면: `gh pr view <n> --repo <owner/repo> --json files -q '.files[].path'`로
     변경 파일을 수집해 `.java`만 남기고, `build/`·`generated`·`openapi` 등 **codegen 산출물은 제외**한다.
     파일 내용은 head SHA(`gh pr view <n> --json headRefOid`) 기준으로
     `gh api "repos/<owner/repo>/contents/<경로>?ref=<headSHA>" -q .content | base64 -d`로 받아
     **실제 줄번호로 인용**한다. 파일이 많으면 패키지 클러스터로 나눠 검토한다.
   - `$ARGUMENTS`가 **경로**면 해당 경로(파일·디렉터리)의 `*.java`.
   - 없으면 git 변경분 — `git diff --name-only HEAD -- '*.java'`에 `git status --porcelain`의 미추적 `*.java`를 더한다.
   대상이 없으면 그렇게 알리고 끝낸다.
2. **규칙 로드**: java-code-consultant 스킬(`java-code-consultant:java-code-consultant`)을
   아직 로드하지 않았다면 Skill 도구로 로드하고, 대상 코드 성격에 맞는 references 파일
   (도메인 로직 / 테스트 / Spring / 구조)을 읽는다.
3. **리뷰 보고**: 파일별로 위반을 **버그 가능성**과 **스타일**로 구분해 버그 가능성부터 보고한다.
   각 지적에 근거 규칙(references 파일·항목)과 `파일:줄` 위치를 인용한다. 통과한 파일은 한 줄로만.
4. **수정 금지**: 이 커맨드는 리뷰 전용이다. 코드를 고치지 말고, 수정은 사용자가 명시적으로
   요청할 때만 진행한다.
