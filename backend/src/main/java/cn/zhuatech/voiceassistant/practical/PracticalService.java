/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.voiceassistant.practical;

import static cn.zhuatech.voiceassistant.practical.PracticalInput.*;

import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@Service
public class PracticalService {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  public Map<String, Object> execute(Map<String, Object> r) {
    validate(r);
    keys(r, "utterance", "minimumMatches", "intents");
    String utterance = normalize(text(r, "utterance", 2000));
    int minimum = integer(r, "minimumMatches", 1, 20);
    Set<String> ids = new HashSet<>();
    List<Map<String, Object>> hits = new ArrayList<>();
    for (var intent : rows(r, "intents", 1, 50)) {
      keys(intent, "id", "phrases", "blockedPhrases", "requiresConfirmation");
      String id = id(intent, "id");
      unique(ids, id);
      boolean confirm = flag(intent, "requiresConfirmation");
      Set<String> phrases = new LinkedHashSet<>();
      for (String phrase : strings(intent, "phrases", 1, 20))
        if (!phrases.add(normalize(phrase))) throw new IllegalArgumentException("归一化短语重复");
      var blocked =
          strings(intent, "blockedPhrases", 0, 20).stream().map(PracticalInput::normalize).toList();
      if (blocked.stream().anyMatch(utterance::contains)) continue;
      var matched = phrases.stream().filter(utterance::contains).toList();
      if (matched.size() >= minimum)
        hits.add(
            Map.of(
                "id",
                id,
                "matchedPhrases",
                matched,
                "matchCount",
                matched.size(),
                "requiresConfirmation",
                confirm));
    }
    hits.sort(
        Comparator.<Map<String, Object>>comparingInt(h -> ((Number) h.get("matchCount")).intValue())
            .reversed()
            .thenComparing(h -> h.get("id").toString()));
    boolean ambiguous =
        hits.size() > 1 && hits.get(0).get("matchCount").equals(hits.get(1).get("matchCount"));
    String state = hits.isEmpty() ? "NO_MATCH" : ambiguous ? "AMBIGUOUS" : "ROUTED";
    return Map.of(
        "state",
        state,
        "selectedIntent",
        state.equals("ROUTED") ? hits.getFirst().get("id") : "",
        "requiresConfirmation",
        !state.equals("ROUTED") || Boolean.TRUE.equals(hits.getFirst().get("requiresConfirmation")),
        "candidates",
        hits,
        "actionExecuted",
        false);
  }
}
