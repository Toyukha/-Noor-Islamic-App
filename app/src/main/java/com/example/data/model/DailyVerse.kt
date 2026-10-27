package com.example.data.model

data class DailyVerse(
  val id: Int,
  val surahName: String,
  val surahNumber: Int,
  val ayahNumber: Int,
  val arabicText: String,
  val transliteration: String,
  val englishTranslation: String,
  val reflectionPrompt: String
)

object VerseCatalogue {
  val VERSES = listOf(
    DailyVerse(
      id = 1,
      surahName = "Al-Baqarah",
      surahNumber = 2,
      ayahNumber = 186,
      arabicText = "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ",
      transliteration = "Wa-idha sa'alaka 'ibadi 'anni fa-inni qarib, ujibu da'wata ad-da'i idha da'an",
      englishTranslation = "And when My servants ask you concerning Me, indeed I am near. I respond to the invocation of the supplicant when he calls upon Me.",
      reflectionPrompt = "No whisper of your heart is unheard. Pause today, raise your hands, and share your deepest hopes with the One who is nearer to you than your jugular vein."
    ),
    DailyVerse(
      id = 2,
      surahName = "Ash-Sharh",
      surahNumber = 94,
      ayahNumber = 5,
      arabicText = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا • إِنَّ مَعَ الْعُسْرِ يُسْرًا",
      transliteration = "Fa-inna ma'al-'usri yusra, inna ma'al-'usri yusra",
      englishTranslation = "For indeed, with hardship [will be] ease. Indeed, with hardship [will be] ease.",
      reflectionPrompt = "Notice that ease does not just follow hardship—it accompanies it. Whatever burden you are carrying right now carries the seed of Divine relief."
    ),
    DailyVerse(
      id = 3,
      surahName = "Ar-Ra'd",
      surahNumber = 13,
      ayahNumber = 28,
      arabicText = "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
      transliteration = "Alladhina amanu wa tatma'innu qulubuhum bi-dhikrillahi, ala bi-dhikrillahi tatma'innul-qulub",
      englishTranslation = "Those who have believed and whose hearts are assured by the remembrance of Allah. Unquestionably, by the remembrance of Allah hearts are assured.",
      reflectionPrompt = "Whenever modern distractions pull your thoughts apart, return to a simple phrase of remembrance: SubhanAllah, Alhamdulillah, Allahu Akbar."
    ),
    DailyVerse(
      id = 4,
      surahName = "At-Talaq",
      surahNumber = 65,
      ayahNumber = 3,
      arabicText = "وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ ۚ وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ",
      transliteration = "Wa yarzuqhu min haythu la yahtasib, wa man yatawakkal 'alallahi fahuwa hasbuh",
      englishTranslation = "And will provide for him from where he does not expect. And whoever relies upon Allah - then He is sufficient for him.",
      reflectionPrompt = "True reliance (Tawakkul) frees the mind from chronic anxiety. Put forth your best effort today and entrust the outcome peacefully to Allah."
    ),
    DailyVerse(
      id = 5,
      surahName = "Al-Anbiya",
      surahNumber = 21,
      ayahNumber = 87,
      arabicText = "لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ",
      transliteration = "La ilaha illa Anta subhanaka inni kuntu minaz-zalimin",
      englishTranslation = "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.",
      reflectionPrompt = "The profound supplication of Prophet Yunus (Jonah) in the depths of the whale. Humble repentance turns deepest trials into miraculous deliverance."
    ),
    DailyVerse(
      id = 6,
      surahName = "Ali 'Imran",
      surahNumber = 3,
      ayahNumber = 139,
      arabicText = "وَلَا تَهِنُوا وَلَا تَحْزَنُوا وَأَنتُمُ الْأَعْلَوْنَ إِن كُنتُم مُّؤْمِنِينَ",
      transliteration = "Wa la tahinu wa la tahzanu wa antumul-a'lawna in kuntum mu'minin",
      englishTranslation = "So do not weaken and do not grieve, and you will be superior if you are [true] believers.",
      reflectionPrompt = "Faith gives you resilience against fatigue and despair. Stand tall in your convictions and hold fast to prayer."
    ),
    DailyVerse(
      id = 7,
      surahName = "Al-Baqarah",
      surahNumber = 2,
      ayahNumber = 152,
      arabicText = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
      transliteration = "Fadhkuruni adhkurkum washkuru li wa la takfurun",
      englishTranslation = "So remember Me; I will remember you. And be grateful to Me and do not deny Me.",
      reflectionPrompt = "Imagine: the Lord of the Heavens and the Earth remembering your name when you mention Him. What honor and blessing!"
    )
  )

  fun getVerseForDayOfYear(dayOfYear: Int): DailyVerse {
    val index = (dayOfYear - 1).coerceAtLeast(0) % VERSES.size
    return VERSES[index]
  }
}
