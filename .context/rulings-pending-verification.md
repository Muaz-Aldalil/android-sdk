# Rulings pending verification (P13)

Hard rule from P13: an agent must NOT edit factual/religious content. These
items are flagged for the project owner to verify with a mufti. They exist
identically in `SH/src/constants/QUIZZES.js` and
`app/src/main/assets/content/quizzes.json` (verified by
`scripts/check-content-parity.js`).

## 1. Asr start time — fiqh-basics Q8 (quizzes.json:1216)

- Q: "متى يبدأ وقت صلاة العصر؟"
- Answer marked correct: "عند الزوال" (option index 1)
- Explanation: "وقت العصر يبدأ من زوال الشمس (الظهيرة) وينتهي عند غروبها"
- Concern: the start of Asr is when an object's shadow equals its length
  (twice its length per the Hanafi school) — not at zawal, which is the start
  of Dhuhr. The explanation appears to conflate the two.

## 2. First ghazwa — quran-basics Q6 (quizzes.json:508)

- Q: "أول غزوة للنبي:"
- Answer marked correct: "بدر الصغرى" (option index 2)
- Explanation: "أول غزوة هي بدر الصغرى (4 شوال 2 هـ)"
- Concern: most sirah sources (Ibn Hisham, al-Waqidi) record the first ghazwa
  as الأبواء (Waddan), with بدر الصغرى later. Verify before changing.

## 3. Congregation prayer — three questions disagree (quizzes.json:980, 1357, 1384)

- islam-pillars Q15: "صلاة الجماعة واجبة على الذكور" → true, "واجبة كفاية على
  الذكور في المسجد — هي من فروض الكفاية" (source: المغني — ابن قدامة)
- fiqh-basics Q18: "هل يجوز للرجل أن يصلي فرضه وحده بدون جماعة؟" → correct:
  "نعم — لا يأثم", "يجوز للرجل أن يصلي فرضه وحده — صلاة الجماعة فرض كفاية
  وليست فرض عين"
- fiqh-basics Q20: "ما حكم صلاة الجماعة على المأموم؟" → correct: "واجب كفاية",
  "صلاة الجماعة واجبة كفاية على الرجال — فرض كفاية إذا صلى بعضهم أجزأ عن الباقي"
- Concern: Q15/Q20 state فرض كفاية (if some pray in congregation the rest are
  absolved), but Q18's "لا يأثم" is unconditional — it does not mention the
  فرض كفاية condition (if nobody prays in congregation, all sin). The three
  answers need a single consistent position.