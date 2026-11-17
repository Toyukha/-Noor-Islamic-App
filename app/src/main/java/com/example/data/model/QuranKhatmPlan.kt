package com.example.data.model

data class DailyQuranSplit(
  val dayNumber: Int,
  val startPage: Int,
  val endPage: Int,
  val totalPages: Int,
  val juzLabel: String,
  val surahsSummary: String,
  val spiritualTheme: String,
  val sampleAyahArabic: String,
  val sampleAyahTranslation: String,
  val sampleSurahAyahRef: String
)

object QuranKhatmPlanCatalogue {

  const val TOTAL_MUSHAF_PAGES = 604
  const val TOTAL_DAYS = 30

  val ALL_30_DAYS: List<DailyQuranSplit> = listOf(
    DailyQuranSplit(
      dayNumber = 1,
      startPage = 1,
      endPage = 20,
      totalPages = 20,
      juzLabel = "Juz 1",
      surahsSummary = "Al-Fatihah (1) & Al-Baqarah (2:1-141)",
      spiritualTheme = "Guidance, The Creation of Humankind, and the Covenant",
      sampleAyahArabic = "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ",
      sampleAyahTranslation = "Guide us to the straight path - The path of those upon whom You have bestowed favor.",
      sampleSurahAyahRef = "Al-Fatihah 1:6-7"
    ),
    DailyQuranSplit(
      dayNumber = 2,
      startPage = 21,
      endPage = 40,
      totalPages = 20,
      juzLabel = "Juz 1 - 2",
      surahsSummary = "Al-Baqarah (2:142-252)",
      spiritualTheme = "The Change of Qiblah, Fasting in Ramadan, and Steadfast Patience",
      sampleAyahArabic = "يَا أَيُّهَا الَّذِينَ آمَنُوا اسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ إِنَّ اللَّهَ مَعَ الصَّابِرِينَ",
      sampleAyahTranslation = "O you who have believed, seek help through patience and prayer. Indeed, Allah is with the patient.",
      sampleSurahAyahRef = "Al-Baqarah 2:153"
    ),
    DailyQuranSplit(
      dayNumber = 3,
      startPage = 41,
      endPage = 60,
      totalPages = 20,
      juzLabel = "Juz 2 - 3",
      surahsSummary = "Al-Baqarah (2:253-286) & Ali 'Imran (3:1-91)",
      spiritualTheme = "Ayat al-Kursi, Charity, and Firmness upon Faith",
      sampleAyahArabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ",
      sampleAyahTranslation = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence.",
      sampleSurahAyahRef = "Al-Baqarah 2:255"
    ),
    DailyQuranSplit(
      dayNumber = 4,
      startPage = 61,
      endPage = 80,
      totalPages = 20,
      juzLabel = "Juz 3 - 4",
      surahsSummary = "Ali 'Imran (3:92-200) & An-Nisa (4:1-23)",
      spiritualTheme = "Unity of Believers, Battle of Uhud, and Family Integrity",
      sampleAyahArabic = "وَاعْتَصِمُوا بِحَبْلِ اللَّهِ جَمِيعًا وَلَا تَفَرَّقُوا",
      sampleAyahTranslation = "And hold firmly to the rope of Allah all together and do not become divided.",
      sampleSurahAyahRef = "Ali 'Imran 3:103"
    ),
    DailyQuranSplit(
      dayNumber = 5,
      startPage = 81,
      endPage = 100,
      totalPages = 20,
      juzLabel = "Juz 4 - 5",
      surahsSummary = "An-Nisa (4:24-147)",
      spiritualTheme = "Justice, Rights of Orphans, Obedience, and Repentance",
      sampleAyahArabic = "إِنَّ اللَّهَ يَأْمُرُكُمْ أَن تُؤَدُّوا الْأَمَانَاتِ إِلَىٰ أَهْلِهَا",
      sampleAyahTranslation = "Indeed, Allah commands you to render trusts to whom they are due and judge with justice.",
      sampleSurahAyahRef = "An-Nisa 4:58"
    ),
    DailyQuranSplit(
      dayNumber = 6,
      startPage = 101,
      endPage = 120,
      totalPages = 20,
      juzLabel = "Juz 5 - 6",
      surahsSummary = "An-Nisa (4:148-176) & Al-Ma'idah (5:1-81)",
      spiritualTheme = "Halal Ordinances, Truthfulness, and Mutual Cooperation in Good",
      sampleAyahArabic = "وَتَعَاوَنُوا عَلَى الْبِرِّ وَالتَّقْوَىٰ وَلَا تَعَاوَنُوا عَلَى الْإِثْمِ وَالْعُدْوَانِ",
      sampleAyahTranslation = "And cooperate in righteousness and piety, but do not cooperate in sin and aggression.",
      sampleSurahAyahRef = "Al-Ma'idah 5:2"
    ),
    DailyQuranSplit(
      dayNumber = 7,
      startPage = 121,
      endPage = 140,
      totalPages = 20,
      juzLabel = "Juz 6 - 7",
      surahsSummary = "Al-Ma'idah (5:82-120) & Al-An'am (6:1-110)",
      spiritualTheme = "Gratitude, Tawhid, and Cosmic Signs of Divine Omnipotence",
      sampleAyahArabic = "وَهُوَ اللَّهُ فِي السَّمَاوَاتِ وَفِي الْأَرْضِ يَعْلَمُ سِرَّكُمْ وَجَهْرَكُمْ",
      sampleAyahTranslation = "And He is Allah in the heavens and the earth. He knows what you conceal and reveal.",
      sampleSurahAyahRef = "Al-An'am 6:3"
    ),
    DailyQuranSplit(
      dayNumber = 8,
      startPage = 141,
      endPage = 160,
      totalPages = 20,
      juzLabel = "Juz 7 - 8",
      surahsSummary = "Al-An'am (6:111-165) & Al-A'raf (7:1-87)",
      spiritualTheme = "Submission of Ibrahim (AS), Prophetic Warnings, and Humility",
      sampleAyahArabic = "قُلْ إِنَّ صَلَاتِي وَنُسُكِي وَمَحْيَايَ وَمَمَاتِي لِلَّهِ رَبِّ الْعَالَمِينَ",
      sampleAyahTranslation = "Say, 'Indeed, my prayer, my rites of sacrifice, my living and my dying are for Allah, Lord of the worlds.'",
      sampleSurahAyahRef = "Al-An'am 6:162"
    ),
    DailyQuranSplit(
      dayNumber = 9,
      startPage = 161,
      endPage = 180,
      totalPages = 20,
      juzLabel = "Juz 8 - 9",
      surahsSummary = "Al-A'raf (7:88-206) & Al-Anfal (8:1-40)",
      spiritualTheme = "Moses and the Children of Israel, Hearts in Divine Awe",
      sampleAyahArabic = "إِنَّمَا الْمُؤْمِنُونَ الَّذِينَ إِذَا ذُكِرَ اللَّهُ وَجِلَتْ قُلُوبُهُمْ",
      sampleAyahTranslation = "The believers are only those who, when Allah is mentioned, their hearts become fearful.",
      sampleSurahAyahRef = "Al-Anfal 8:2"
    ),
    DailyQuranSplit(
      dayNumber = 10,
      startPage = 181,
      endPage = 200,
      totalPages = 20,
      juzLabel = "Juz 9 - 10",
      surahsSummary = "Al-Anfal (8:41-75) & At-Tawbah (9:1-92)",
      spiritualTheme = "Tawakkul, Sincere Repentance, and Devotion to Allah's Cause",
      sampleAyahArabic = "وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَإِنَّ اللَّهَ عَزِيزٌ حَكِيمٌ",
      sampleAyahTranslation = "And whoever relies upon Allah - then indeed, Allah is Exalted in Might and Wise.",
      sampleSurahAyahRef = "Al-Anfal 8:49"
    ),
    DailyQuranSplit(
      dayNumber = 11,
      startPage = 201,
      endPage = 220,
      totalPages = 20,
      juzLabel = "Juz 10 - 11",
      surahsSummary = "At-Tawbah (9:93-129) & Yunus (10:1-109)",
      spiritualTheme = "The Prophet's Mercy, Divine Healing, and Peaceful Certainty",
      sampleAyahArabic = "يَا أَيُّهَا النَّاسُ قَدْ جَاءَتْكُم مَّوْعِظَةٌ مِّن رَّبِّكُمْ وَشِفَاءٌ لِّمَا فِي الصُّدُورِ",
      sampleAyahTranslation = "O mankind, there has to come to you instruction from your Lord and healing for what is in the breasts.",
      sampleSurahAyahRef = "Yunus 10:57"
    ),
    DailyQuranSplit(
      dayNumber = 12,
      startPage = 221,
      endPage = 240,
      totalPages = 20,
      juzLabel = "Juz 11 - 12",
      surahsSummary = "Hud (11:1-123) & Yusuf (12:1-52)",
      spiritualTheme = "Steadfastness of the Messengers and the Best of Stories (Yusuf)",
      sampleAyahArabic = "فَاسْتَقِمْ كَمَا أُمِرْتَ وَمَن تَابَ مَعَكَ وَلَا تَطْغَوْا",
      sampleAyahTranslation = "So remain on a right course as you have been commanded, along with those who have turned back with you.",
      sampleSurahAyahRef = "Hud 11:112"
    ),
    DailyQuranSplit(
      dayNumber = 13,
      startPage = 241,
      endPage = 260,
      totalPages = 20,
      juzLabel = "Juz 12 - 13",
      surahsSummary = "Yusuf (12:53-111), Ar-Ra'd (13), & Ibrahim (14)",
      spiritualTheme = "Patience in Adversity, Hearts Finding Peace in Dhikr",
      sampleAyahArabic = "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
      sampleAyahTranslation = "Those who have believed and whose hearts find rest in the remembrance of Allah. Unquestionably, by the remembrance of Allah hearts are assured.",
      sampleSurahAyahRef = "Ar-Ra'd 13:28"
    ),
    DailyQuranSplit(
      dayNumber = 14,
      startPage = 261,
      endPage = 280,
      totalPages = 20,
      juzLabel = "Juz 13 - 14",
      surahsSummary = "Al-Hijr (15) & An-Nahl (16:1-128)",
      spiritualTheme = "Divine Protection of Quran, Countless Blessings (An-Nahl)",
      sampleAyahArabic = "وَإِن تَعُدُّوا نِعْمَةَ اللَّهِ لَا تُحْصُوهَا ۗ إِنَّ اللَّهَ لَغَفُورٌ رَّحِيمٌ",
      sampleAyahTranslation = "And if you should count the blessings of Allah, you could not enumerate them. Indeed, Allah is Forgiving and Merciful.",
      sampleSurahAyahRef = "An-Nahl 16:18"
    ),
    DailyQuranSplit(
      dayNumber = 15,
      startPage = 281,
      endPage = 300,
      totalPages = 20,
      juzLabel = "Juz 14 - 15",
      surahsSummary = "Al-Isra (17) & Al-Kahf (18:1-74)",
      spiritualTheme = "The Night Journey (Isra), Honoring Parents, Trials of Faith",
      sampleAyahArabic = "وَبِالْوَالِدَيْنِ إِحْسَانًا ۚ إِمَّا يَبْلُغَنَّ عِندَكَ الْكِبَرَ أَحَدُهُمَا أَوْ كِلَاهُمَا فَلَا تَقُل لَّهُمَا أُفٍّ",
      sampleAyahTranslation = "And to parents, good treatment. Whether one or both of them reach old age, do not say to them, 'Uff'.",
      sampleSurahAyahRef = "Al-Isra 17:23"
    ),
    DailyQuranSplit(
      dayNumber = 16,
      startPage = 301,
      endPage = 320,
      totalPages = 20,
      juzLabel = "Juz 15 - 16",
      surahsSummary = "Al-Kahf (18:75-110), Maryam (19), & Ta-Ha (20:1-135)",
      spiritualTheme = "Dhul-Qarnayn, Supplication of Zakariyya, Peace granted to Maryam",
      sampleAyahArabic = "قَالَ رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي",
      sampleAyahTranslation = "Moses said, 'My Lord, expand for me my breast and ease for me my task.'",
      sampleSurahAyahRef = "Ta-Ha 20:25-26"
    ),
    DailyQuranSplit(
      dayNumber = 17,
      startPage = 321,
      endPage = 340,
      totalPages = 20,
      juzLabel = "Juz 16 - 17",
      surahsSummary = "Al-Anbiya (21) & Al-Hajj (22:1-78)",
      spiritualTheme = "Supplications of the Prophets, Pilgrimage, and Sacrificial Devotion",
      sampleAyahArabic = "وَذَا النُّونِ إِذ ذَّهَبَ مُغَاضِبًا ... لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ",
      sampleAyahTranslation = "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.",
      sampleSurahAyahRef = "Al-Anbiya 21:87"
    ),
    DailyQuranSplit(
      dayNumber = 18,
      startPage = 341,
      endPage = 360,
      totalPages = 20,
      juzLabel = "Juz 17 - 18",
      surahsSummary = "Al-Mu'minun (23) & An-Nur (24:1-64)",
      spiritualTheme = "Attributes of Victorious Believers, The Verse of Light (Ayat An-Nur)",
      sampleAyahArabic = "قَدْ أَفْلَحَ الْمُؤْمِنُونَ الَّذِينَ هُمْ فِي صَلَاتِهِمْ خَاشِعُونَ",
      sampleAyahTranslation = "Certainly will the believers have succeeded: They who are during their prayer humbly submissive.",
      sampleSurahAyahRef = "Al-Mu'minun 23:1-2"
    ),
    DailyQuranSplit(
      dayNumber = 19,
      startPage = 361,
      endPage = 380,
      totalPages = 20,
      juzLabel = "Juz 18 - 19",
      surahsSummary = "Al-Furqan (25) & Ash-Shu'ara (26:1-227)",
      spiritualTheme = "Servants of the Most Merciful ('Ibad ar-Rahman)",
      sampleAyahArabic = "وَعِبَادُ الرَّحْمَٰنِ الَّذِينَ يَمْشُونَ عَلَى الْأَرْضِ هَوْنًا وَإِذَا خَاطَبَهُمُ الْجَاهِلُونَ قَالُوا سَلَامًا",
      sampleAyahTranslation = "And the servants of the Most Merciful are those who walk upon the earth easily, and when the ignorant address them, they say, 'Peace.'",
      sampleSurahAyahRef = "Al-Furqan 25:63"
    ),
    DailyQuranSplit(
      dayNumber = 20,
      startPage = 381,
      endPage = 400,
      totalPages = 20,
      juzLabel = "Juz 19 - 20",
      surahsSummary = "An-Naml (27) & Al-Qasas (28:1-88)",
      spiritualTheme = "Kingdom of Sulayman, Gratitude, and Trust in Divine Promise",
      sampleAyahArabic = "رَبِّ أَوْزِعْنِي أَنْ أَشْكُرَ نِعْمَتَكَ الَّتِي أَنْعَمْتَ عَلَيَّ وَعَلَىٰ وَالِدَيَّ",
      sampleAyahTranslation = "My Lord, enable me to be grateful for Your favor which You have bestowed upon me and upon my parents.",
      sampleSurahAyahRef = "An-Naml 27:19"
    ),
    DailyQuranSplit(
      dayNumber = 21,
      startPage = 401,
      endPage = 420,
      totalPages = 20,
      juzLabel = "Juz 20 - 21",
      surahsSummary = "Al-'Ankabut (29) & Ar-Rum (30:1-60)",
      spiritualTheme = "Tests of Faith, Prayer Restraining Transgression, Signs in Creation",
      sampleAyahArabic = "اتْلُ مَا أُوحِيَ إِلَيْكَ مِنَ الْكِتَابِ وَأَقِمِ الصَّلَاةَ ۖ إِنَّ الصَّلَاةَ تَنْهَىٰ عَنِ الْفَحْشَاءِ وَالْمُنكَرِ",
      sampleAyahTranslation = "Recite what has been revealed to you of the Book and establish prayer. Indeed, prayer prohibits immorality and wrongdoing.",
      sampleSurahAyahRef = "Al-'Ankabut 29:45"
    ),
    DailyQuranSplit(
      dayNumber = 22,
      startPage = 421,
      endPage = 440,
      totalPages = 20,
      juzLabel = "Juz 21 - 22",
      surahsSummary = "Luqman (31), As-Sajdah (32), & Al-Ahzab (33:1-50)",
      spiritualTheme = "Wisdom of Luqman to His Son, Night Prostrations, Noble Character",
      sampleAyahArabic = "يَا بُنَيَّ أَقِمِ الصَّلَاةَ وَأْمُرْ بِالْمَعْرُوفِ وَانْهَ عَنِ الْمُنكَرِ وَاصْبِرْ عَلَىٰ مَا أَصَابَكَ",
      sampleAyahTranslation = "O my son, establish prayer, enjoin what is right, forbid what is wrong, and be patient over what befalls you.",
      sampleSurahAyahRef = "Luqman 31:17"
    ),
    DailyQuranSplit(
      dayNumber = 23,
      startPage = 441,
      endPage = 460,
      totalPages = 20,
      juzLabel = "Juz 22 - 23",
      surahsSummary = "Al-Ahzab (33:51-73), Saba (34), & Fatir (35)",
      spiritualTheme = "Salawat upon the Prophet, Dawud & Sulayman, Originator of the Heavens",
      sampleAyahArabic = "إِنَّ اللَّهَ وَمَلَائِكَتَهُ يُصَلُّونَ عَلَى النَّبِيِّ ۚ يَا أَيُّهَا الَّذِينَ آمَنُوا صَلُّوا عَلَيْهِ وَسَلِّمُوا تَسْلِيمًا",
      sampleAyahTranslation = "Indeed, Allah and His angels confer blessings upon the Prophet. O you who have believed, ask blessings upon him and salute him with peace.",
      sampleSurahAyahRef = "Al-Ahzab 33:56"
    ),
    DailyQuranSplit(
      dayNumber = 24,
      startPage = 461,
      endPage = 480,
      totalPages = 20,
      juzLabel = "Juz 23 - 24",
      surahsSummary = "Ya-Sin (36), As-Saffat (37), & Sad (38:1-88)",
      spiritualTheme = "Heart of the Quran (Ya-Sin), Sacrifice of Isma'il, and Ayyub's Patience",
      sampleAyahArabic = "إِنَّمَا أَمْرُهُ إِذَا أَرَادَ شَيْئًا أَن يَقُولَ لَهُ كُن فَيَكُونُ",
      sampleAyahTranslation = "His command is only when He intends a thing that He says to it, 'Be,' and it is.",
      sampleSurahAyahRef = "Ya-Sin 36:82"
    ),
    DailyQuranSplit(
      dayNumber = 25,
      startPage = 481,
      endPage = 500,
      totalPages = 20,
      juzLabel = "Juz 24 - 25",
      surahsSummary = "Az-Zumar (39), Ghafir (40), & Fussilat (41:1-46)",
      spiritualTheme = "Despair Not of Allah's Mercy, Calling Upon Him, Goodness Repelling Evil",
      sampleAyahArabic = "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنفُسِهِمْ لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ ۚ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا",
      sampleAyahTranslation = "Say, 'O My servants who have transgressed against themselves, do not despair of the mercy of Allah. Indeed, Allah forgives all sins.'",
      sampleSurahAyahRef = "Az-Zumar 39:53"
    ),
    DailyQuranSplit(
      dayNumber = 26,
      startPage = 501,
      endPage = 520,
      totalPages = 20,
      juzLabel = "Juz 25 - 26",
      surahsSummary = "Ash-Shura (42), Az-Zukhruf (43), Ad-Dukhan (44), & Al-Jathiyah (45)",
      spiritualTheme = "Consultation, Honor in Islam, The Night of Decree, Subservient Heavens",
      sampleAyahArabic = "وَهُوَ الَّذِي يَقْبَلُ التَّوْبَةَ عَنْ عِبَادِهِ وَيَعْفُو عَنِ السَّيِّئَاتِ وَيَعْلَمُ مَا تَفْعَلُونَ",
      sampleAyahTranslation = "And it is He who accepts repentance from His servants and pardons misdeeds, and He knows what you do.",
      sampleSurahAyahRef = "Ash-Shura 42:25"
    ),
    DailyQuranSplit(
      dayNumber = 27,
      startPage = 521,
      endPage = 540,
      totalPages = 20,
      juzLabel = "Juz 26 - 27",
      surahsSummary = "Al-Ahqaf (46), Muhammad (47), Al-Fath (48), & Al-Hujurat (49)",
      spiritualTheme = "Manifest Victory (Al-Fath), Brotherhood, and Avoiding Backbiting",
      sampleAyahArabic = "يَا أَيُّهَا النَّاسُ إِنَّا خَلَقْنَاكُم مِّن ذَكَرٍ وَأُنثَىٰ وَجَعَلْنَاكُمْ شُعُوبًا وَقَبَائِلَ لِتَعَارَفُوا ۚ إِنَّ أَكْرَمَكُمْ عِندَ اللَّهِ أَتْقَاكُمْ",
      sampleAyahTranslation = "O mankind, We created you from male and female and made you peoples and tribes that you may know one another. The most noble of you is the most righteous.",
      sampleSurahAyahRef = "Al-Hujurat 49:13"
    ),
    DailyQuranSplit(
      dayNumber = 28,
      startPage = 541,
      endPage = 560,
      totalPages = 20,
      juzLabel = "Juz 27 - 28",
      surahsSummary = "Qaf (50) through Al-Hadid (57)",
      spiritualTheme = "Proximity of Allah, Ar-Rahman, Al-Waqi'ah, and Light for the Believers",
      sampleAyahArabic = "وَنَحْنُ أَقْرَبُ إِلَيْهِ مِنْ حَبْلِ الْوَرِيدِ",
      sampleAyahTranslation = "And We are closer to him than his jugular vein.",
      sampleSurahAyahRef = "Qaf 50:16"
    ),
    DailyQuranSplit(
      dayNumber = 29,
      startPage = 561,
      endPage = 580,
      totalPages = 20,
      juzLabel = "Juz 28 - 29",
      surahsSummary = "Al-Mujadila (58) through Al-Mursalat (77) (incl. Al-Mulk)",
      spiritualTheme = "Surah Al-Mulk (The Protector), Al-Qalam, and True Spiritual Reliance",
      sampleAyahArabic = "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
      sampleAyahTranslation = "Blessed is He in whose hand is dominion, and He is over all things competent.",
      sampleSurahAyahRef = "Al-Mulk 67:1"
    ),
    DailyQuranSplit(
      dayNumber = 30,
      startPage = 581,
      endPage = 604,
      totalPages = 24,
      juzLabel = "Juz 30 (Amma)",
      surahsSummary = "An-Naba (78) through An-Nas (114) — Complete Quran Khatm!",
      spiritualTheme = "The Grand Announcement, Al-Ikhlas, Al-Falaq, and An-Nas: Khatm Du'a",
      sampleAyahArabic = "قُلْ هُوَ اللَّهُ أَحَدٌ اللَّهُ الصَّمَدُ لَمْ يَلِدْ وَلَمْ يُولَدْ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
      sampleAyahTranslation = "Say, 'He is Allah, [who is] One. Allah, the Eternal Refuge. He neither begets nor is born, nor is there to Him any equivalent.'",
      sampleSurahAyahRef = "Al-Ikhlas 112:1-4"
    )
  )

  fun getTotalDaysForDuration(months: Int): Int {
    return months.coerceIn(1, 3) * 30
  }

  fun getSplitForDayAndDuration(day: Int, months: Int = 1): DailyQuranSplit {
    val validMonths = months.coerceIn(1, 3)
    val totalDays = validMonths * 30
    val clampedDay = day.coerceIn(1, totalDays)

    if (validMonths == 1 && clampedDay in 1..30) {
      return ALL_30_DAYS[clampedDay - 1]
    }

    val pagesPerDay = TOTAL_MUSHAF_PAGES.toDouble() / totalDays
    val startPage = ((clampedDay - 1) * pagesPerDay).toInt() + 1
    val endPage = if (clampedDay == totalDays) TOTAL_MUSHAF_PAGES else (clampedDay * pagesPerDay).toInt().coerceAtLeast(startPage)
    val totalPages = (endPage - startPage + 1).coerceAtLeast(1)
    val approxJuz = (((startPage - 1) / 20) + 1).coerceIn(1, 30)

    return DailyQuranSplit(
      dayNumber = clampedDay,
      startPage = startPage,
      endPage = endPage,
      totalPages = totalPages,
      juzLabel = "Juz $approxJuz",
      surahsSummary = "Target: Pages $startPage – $endPage ($totalPages pages today)",
      spiritualTheme = "Completing the Holy Quran in $validMonths Month${if (validMonths > 1) "s" else ""}",
      sampleAyahArabic = "وَرَتِّلِ الْقُرْآنَ تَرْتِيلًا",
      sampleAyahTranslation = "And recite the Quran with measured recitation.",
      sampleSurahAyahRef = "Al-Muzzammil 73:4"
    )
  }

  fun getSplitForDay(day: Int): DailyQuranSplit {
    return getSplitForDayAndDuration(day, 1)
  }
}
