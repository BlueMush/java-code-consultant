#!/usr/bin/env bash
# java-code-consultant 스킬 발동 스모크 테스트.
# 위반이 심긴 fixtures/ 를 headless Claude 로 리뷰시켜 핵심 위반 검출 여부를 키워드로 확인한다.
# 사용: tests/smoke.sh   (claude CLI 필요, 1~3분 소요)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PLUGIN="$ROOT/plugins/java-code-consultant"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
cp "$ROOT"/tests/fixtures/*.java "$WORK/"

echo "[smoke] 리뷰 실행 중... (작업 디렉터리: $WORK)"
OUT="$(cd "$WORK" && claude --plugin-dir "$PLUGIN" \
  -p "PointController.java, PointService.java, PointServiceTest.java 를 리뷰해줘. 수정하지 말고 지적만." \
  --permission-mode plan 2>&1)"

echo "$OUT"
echo "----------------------------------------"

fail=0
check() { # check <라벨> <정규식>
  if grep -qiE "$2" <<<"$OUT"; then
    echo "[PASS] $1"
  else
    echo "[FAIL] $1  (패턴: $2)"
    fail=1
  fi
}

check "double 금액 지적"          "double|부동소수"
check "필드 주입 지적"            "Autowired|생성자 주입"
check "Optional 오용 지적"        "Optional"
check "null 반환 지적"            "null"
check "JUnit assertion 지적"      "assertEquals|AssertJ|assertThat"
check "@MockBean deprecated 지적" "MockitoBean|MockBean"
check "List.of\(null\) 함정 지적" "List\.of|무효"
check "엔티티 응답 노출 지적"     "엔티티|JpaEntity"
check "컨트롤러 로직 지적"        "분기|비즈니스 로직|유스케이스|UseCase"
check "URL/네이밍 지적"           "getPoint|동사|네이밍|이름"
check "버그/스타일 구분 준수"     "버그"

if [ "$fail" -eq 0 ]; then
  echo "[smoke] 전체 통과"
else
  echo "[smoke] 실패 — 위 FAIL 항목과 리뷰 출력을 비교할 것 (기대치: tests/EXPECTED.md)"
fi
exit "$fail"
