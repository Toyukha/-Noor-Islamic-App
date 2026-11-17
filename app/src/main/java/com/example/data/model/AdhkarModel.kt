package com.example.data.model

enum class AdhkarPeriod(val title: String, val arabicTitle: String) {
  MORNING("Morning Adhkar", "أذكار الصباح"),
  EVENING("Evening Adhkar", "أذكار المساء")
}

data class AdhkarItem(
  val id: String,
  val period: AdhkarPeriod,
  val title: String,
  val arabicText: String,
  val englishTranslation: String,
  val virtue: String,
  val repeatCount: Int = 1,
  val audioUrl: String = ""
)

object AdhkarCatalogue {

  val morningAdhkar: List<AdhkarItem> = listOf(
    AdhkarItem(
      id = "m_ayat_kursi",
      period = AdhkarPeriod.MORNING,
      title = "Ayat al-Kursi (The Throne Verse)",
      arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
      englishTranslation = "Allah! There is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.",
      virtue = "Whoever recites this when he rises in the morning will be protected from the jinn until evening. (Al-Hakim, Sahih)",
      repeatCount = 1,
      audioUrl = "https://server7.mp3quran.net/afs/002255.mp3"
    ),
    AdhkarItem(
      id = "m_muawwidhat",
      period = AdhkarPeriod.MORNING,
      title = "Al-Mu'awwidhat (Surahs Al-Ikhlas, Al-Falaq, An-Nas)",
      arabicText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nقُلْ هُوَ اللَّهُ أَحَدٌ ۞ اللَّهُ الصَّمَدُ ۞ لَمْ يَلِدْ وَلَمْ يُولَدْ ۞ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ\n\nبِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nقُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۞ مِن شَرِّ مَا خَلَقَ ۞ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ۞ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۞ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ\n\nبِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nقُلْ أَعُوذُ بِرَبِّ النَّاسِ ۞ مَلِكِ النَّاسِ ۞ إِلَٰهِ النَّاسِ ۞ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۞ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۞ مِنَ الْجِنَّةِ وَالنَّاسِ",
      englishTranslation = "Recite Surah Al-Ikhlas, Surah Al-Falaq, and Surah An-Nas three times in the morning and evening.",
      virtue = "Recite them three times in the morning and evening, and they will suffice you against everything. (At-Tirmidhi, Abu Dawud)",
      repeatCount = 3,
      audioUrl = "https://server7.mp3quran.net/afs/112.mp3"
    ),
    AdhkarItem(
      id = "m_asbahna",
      period = AdhkarPeriod.MORNING,
      title = "Asbahna wa Asbahal-Mulk",
      arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَـهَ إِلاَّ اللهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَـذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَـذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ، وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ",
      englishTranslation = "We have reached the morning and the dominion belongs to Allah, all praise is due to Allah. None has the right to be worshipped but Allah alone, Who has no partner. His is the dominion and His is the praise, and He has power over all things. My Lord, I ask You for the good of this day and what comes after it, and I seek refuge in You from the evil of this day and what comes after it. My Lord, I seek refuge in You from laziness and the hardships of old age. My Lord, I seek refuge in You from punishment in the Fire and punishment in the grave.",
      virtue = "A comprehensive daily renewal of reliance upon Allah. (Sahih Muslim)",
      repeatCount = 1,
      audioUrl = "https://server7.mp3quran.net/afs/067.mp3"
    ),
    AdhkarItem(
      id = "m_sayyid_istighfar",
      period = AdhkarPeriod.MORNING,
      title = "Sayyid al-Istighfar (Chief of Forgiveness)",
      arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ",
      englishTranslation = "O Allah, You are my Lord, none has the right to be worshipped but You. You created me and I am Your slave, and I abide by Your covenant and promise as best I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favors upon me, and I acknowledge my sin, so forgive me, for none can forgive sins except You.",
      virtue = "Whoever recites this with conviction in the morning and dies before evening will be among the people of Paradise. (Sahih Al-Bukhari)",
      repeatCount = 1,
      audioUrl = "https://server7.mp3quran.net/afs/067.mp3"
    ),
    AdhkarItem(
      id = "m_afiyah",
      period = AdhkarPeriod.MORNING,
      title = "Supplication for Well-Being & Security",
      arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالآخِرَةِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي دِينِي وَدُنْيَايَ وَأَهْلِي وَمَالِي، اللَّهُمَّ اسْتُرْ عَوْرَاتِي، وَآمِنْ رَوْعَاتِي، اللَّهُمَّ احْفَظْنِي مِنْ بَيْنِ يَدَيَّ، وَمِنْ خَلْفِي، وَعَنْ يَمِينِي، وَعَنْ شِمَالِي، وَمِنْ فَوْقِي، وَأَعُوذُ بِعَظَمَتِكَ أَنْ أُغْتَالَ مِنْ تَحْتِي",
      englishTranslation = "O Allah, I ask You for pardon and well-being in this life and the next. O Allah, I ask You for pardon and well-being in my religion, worldly affairs, family, and wealth. O Allah, conceal my faults and calm my fears. O Allah, protect me from in front of me and behind me, on my right and on my left, and from above me; and I seek refuge in Your greatness from being assassinated from beneath me.",
      virtue = "The Messenger of Allah ﷺ never omitted these supplications in the morning and evening. (Abu Dawud, Ibn Majah)",
      repeatCount = 1,
      audioUrl = "https://server7.mp3quran.net/afs/067.mp3"
    ),
    AdhkarItem(
      id = "m_bismillah",
      period = AdhkarPeriod.MORNING,
      title = "Shield Against All Harm",
      arabicText = "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
      englishTranslation = "In the Name of Allah, with Whose Name nothing can cause harm in the earth or in the heaven, and He is the All-Hearing, the All-Knowing.",
      virtue = "Whoever says this three times in the morning and evening, nothing will harm him. (At-Tirmidhi, Abu Dawud)",
      repeatCount = 3,
      audioUrl = "https://server7.mp3quran.net/afs/067.mp3"
    ),
    AdhkarItem(
      id = "m_raditu",
      period = AdhkarPeriod.MORNING,
      title = "Satisfaction with Allah & Islam",
      arabicText = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالإِسْلاَمِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
      englishTranslation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad ﷺ as my Prophet.",
      virtue = "Whoever recites this three times in the morning and evening, it is a duty upon Allah to please him on the Day of Resurrection. (At-Tirmidhi)",
      repeatCount = 3,
      audioUrl = "https://server7.mp3quran.net/afs/067.mp3"
    ),
    AdhkarItem(
      id = "m_ya_hayyu",
      period = AdhkarPeriod.MORNING,
      title = "Calling upon the Ever-Living Sustainer",
      arabicText = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلاَ تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ",
      englishTranslation = "O Ever-Living, O Sustainer, by Your mercy I seek assistance; rectify for me all of my affairs and do not leave me in charge of myself even for the blink of an eye.",
      virtue = "The Prophet ﷺ advised Fatimah (RA) to say this every morning and evening. (An-Nasa'i, Sahih)",
      repeatCount = 1,
      audioUrl = "https://server7.mp3quran.net/afs/067.mp3"
    ),
    AdhkarItem(
      id = "m_subhan_allah",
      period = AdhkarPeriod.MORNING,
      title = "Subhan Allahi wa bi-Hamdihi (100 Times)",
      arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
      englishTranslation = "Glory is to Allah and praise is to Him.",
      virtue = "Whoever says this 100 times a day, his sins will be forgiven even if they were like the foam of the sea. (Sahih Al-Bukhari)",
      repeatCount = 100,
      audioUrl = "https://server7.mp3quran.net/afs/067.mp3"
    )
  )

  val eveningAdhkar: List<AdhkarItem> = listOf(
    AdhkarItem(
      id = "e_ayat_kursi",
      period = AdhkarPeriod.EVENING,
      title = "Ayat al-Kursi (The Throne Verse)",
      arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
      englishTranslation = "Allah! There is no deity except Him, the Ever-Living, the Sustainer of all existence...",
      virtue = "Whoever recites this when he reaches the evening will be protected from the jinn until morning. (Al-Hakim)",
      repeatCount = 1,
      audioUrl = "https://server7.mp3quran.net/afs/002255.mp3"
    ),
    AdhkarItem(
      id = "e_muawwidhat",
      period = AdhkarPeriod.EVENING,
      title = "Al-Mu'awwidhat (Surahs Al-Ikhlas, Al-Falaq, An-Nas)",
      arabicText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nقُلْ هُوَ اللَّهُ أَحَدٌ ۞ اللَّهُ الصَّمَدُ ۞ لَمْ يَلِدْ وَلَمْ يُولَدْ ۞ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ\n\nبِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nقُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۞ مِن شَرِّ مَا خَلَقَ ۞ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ۞ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۞ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ\n\nبِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nقُلْ أَعُوذُ بِرَبِّ النَّاسِ ۞ مَلِكِ النَّاسِ ۞ إِلَٰهِ النَّاسِ ۞ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۞ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۞ مِنَ الْجِنَّةِ وَالنَّاسِ",
      englishTranslation = "Recite Surah Al-Ikhlas, Surah Al-Falaq, and Surah An-Nas three times in the evening.",
      virtue = "They will suffice you against everything. (At-Tirmidhi)",
      repeatCount = 3,
      audioUrl = "https://server7.mp3quran.net/afs/112.mp3"
    ),
    AdhkarItem(
      id = "e_amsayna",
      period = AdhkarPeriod.EVENING,
      title = "Amsayna wa Amsal-Mulk",
      arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَـهَ إِلاَّ اللهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَـذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَـذِهِ اللَّيْلَةِ وَشَرِّ مَا بَعْدَهَا، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ، وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ",
      englishTranslation = "We have reached the evening and the dominion belongs to Allah, all praise is due to Allah. None has the right to be worshipped but Allah alone, Who has no partner. His is the dominion and His is the praise, and He has power over all things. My Lord, I ask You for the good of this night and what comes after it, and I seek refuge in You from the evil of this night and what comes after it. My Lord, I seek refuge in You from laziness and the hardships of old age. My Lord, I seek refuge in You from punishment in the Fire and punishment in the grave.",
      virtue = "A prophetic fortress of the night. (Sahih Muslim)",
      repeatCount = 1,
      audioUrl = "https://server7.mp3quran.net/afs/067.mp3"
    ),
    AdhkarItem(
      id = "e_sayyid_istighfar",
      period = AdhkarPeriod.EVENING,
      title = "Sayyid al-Istighfar (Chief of Forgiveness)",
      arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ",
      englishTranslation = "O Allah, You are my Lord, none has the right to be worshipped but You. You created me and I am Your slave, and I abide by Your covenant and promise as best I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favors upon me, and I acknowledge my sin, so forgive me, for none can forgive sins except You.",
      virtue = "Whoever recites this with conviction in the evening and dies that night will enter Paradise. (Sahih Al-Bukhari)",
      repeatCount = 1,
      audioUrl = "https://server7.mp3quran.net/afs/067.mp3"
    ),
    AdhkarItem(
      id = "e_kalimat_tammati",
      period = AdhkarPeriod.EVENING,
      title = "Seeking Protection in Allah's Perfect Words",
      arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
      englishTranslation = "I seek refuge in the Perfect Words of Allah from the evil of what He has created.",
      virtue = "Whoever recites this three times in the evening, no harmful sting or fever will afflict him that night. (Sahih Muslim)",
      repeatCount = 3,
      audioUrl = "https://server7.mp3quran.net/afs/067.mp3"
    ),
    AdhkarItem(
      id = "e_bismillah",
      period = AdhkarPeriod.EVENING,
      title = "Shield Against All Harm",
      arabicText = "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
      englishTranslation = "In the Name of Allah, with Whose Name nothing can cause harm in the earth or in the heaven, and He is the All-Hearing, the All-Knowing.",
      virtue = "Nothing will harm him that night. (At-Tirmidhi)",
      repeatCount = 3,
      audioUrl = "https://server7.mp3quran.net/afs/067.mp3"
    ),
    AdhkarItem(
      id = "e_raditu",
      period = AdhkarPeriod.EVENING,
      title = "Satisfaction with Allah & Islam",
      arabicText = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالإِسْلاَمِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
      englishTranslation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad ﷺ as my Prophet.",
      virtue = "A duty upon Allah to please him on the Day of Resurrection. (At-Tirmidhi)",
      repeatCount = 3,
      audioUrl = "https://server7.mp3quran.net/afs/067.mp3"
    ),
    AdhkarItem(
      id = "e_subhan_allah",
      period = AdhkarPeriod.EVENING,
      title = "Subhan Allahi wa bi-Hamdihi (100 Times)",
      arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
      englishTranslation = "Glory is to Allah and praise is to Him.",
      virtue = "No one will come on the Day of Resurrection with anything better except one who said the same or more. (Sahih Muslim)",
      repeatCount = 100,
      audioUrl = "https://server7.mp3quran.net/afs/067.mp3"
    )
  )

  fun getAdhkarForPeriod(period: AdhkarPeriod): List<AdhkarItem> {
    return if (period == AdhkarPeriod.MORNING) morningAdhkar else eveningAdhkar
  }
}
