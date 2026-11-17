package com.example.data.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Comprehensive offline directory of worldwide cities and regions for Islamic prayer calculations.
 * Covers major metropolitan areas, historic Islamic cultural hubs, and cities across every continent.
 */
object GlobalCityDirectory {

  val REGIONS = listOf(
    "All",
    "Middle East",
    "Africa",
    "Asia",
    "Europe",
    "Americas",
    "Oceania"
  )

  val ALL_CITIES: List<CityLocation> = listOf(
    // --- MIDDLE EAST ---
    CityLocation("Mecca", "Saudi Arabia", 21.4225, 39.8262, 3.0, "Middle East"),
    CityLocation("Medina", "Saudi Arabia", 24.4672, 39.6111, 3.0, "Middle East"),
    CityLocation("Riyadh", "Saudi Arabia", 24.7136, 46.6753, 3.0, "Middle East"),
    CityLocation("Jeddah", "Saudi Arabia", 21.5433, 39.1728, 3.0, "Middle East"),
    CityLocation("Dammam", "Saudi Arabia", 26.4207, 50.0888, 3.0, "Middle East"),
    CityLocation("Khobar", "Saudi Arabia", 26.2172, 50.1971, 3.0, "Middle East"),
    CityLocation("Taif", "Saudi Arabia", 21.2854, 40.4222, 3.0, "Middle East"),
    CityLocation("Tabuk", "Saudi Arabia", 28.3835, 36.5662, 3.0, "Middle East"),
    CityLocation("Buraydah", "Saudi Arabia", 26.3592, 43.9818, 3.0, "Middle East"),
    CityLocation("Abha", "Saudi Arabia", 18.2164, 42.5053, 3.0, "Middle East"),
    CityLocation("Dubai", "United Arab Emirates", 25.2048, 55.2708, 4.0, "Middle East"),
    CityLocation("Abu Dhabi", "United Arab Emirates", 24.4539, 54.3773, 4.0, "Middle East"),
    CityLocation("Sharjah", "United Arab Emirates", 25.3573, 55.4033, 4.0, "Middle East"),
    CityLocation("Ajman", "United Arab Emirates", 25.4052, 55.5136, 4.0, "Middle East"),
    CityLocation("Doha", "Qatar", 25.2854, 51.5310, 3.0, "Middle East"),
    CityLocation("Al Rayyan", "Qatar", 25.2919, 51.4244, 3.0, "Middle East"),
    CityLocation("Kuwait City", "Kuwait", 29.3759, 47.9774, 3.0, "Middle East"),
    CityLocation("Hawalli", "Kuwait", 29.3328, 48.0281, 3.0, "Middle East"),
    CityLocation("Manama", "Bahrain", 26.2285, 50.5860, 3.0, "Middle East"),
    CityLocation("Muharraq", "Bahrain", 26.2572, 50.6119, 3.0, "Middle East"),
    CityLocation("Muscat", "Oman", 23.5880, 58.3829, 4.0, "Middle East"),
    CityLocation("Salalah", "Oman", 17.0151, 54.0924, 4.0, "Middle East"),
    CityLocation("Nizwa", "Oman", 22.9333, 57.5333, 4.0, "Middle East"),
    CityLocation("Amman", "Jordan", 31.9454, 35.9284, 3.0, "Middle East"),
    CityLocation("Zarqa", "Jordan", 32.0728, 36.0880, 3.0, "Middle East"),
    CityLocation("Irbid", "Jordan", 32.5568, 35.8469, 3.0, "Middle East"),
    CityLocation("Beirut", "Lebanon", 33.8938, 35.5018, 2.0, "Middle East"),
    CityLocation("Tripoli", "Lebanon", 34.4367, 35.8497, 2.0, "Middle East"),
    CityLocation("Sidon", "Lebanon", 33.5631, 35.3689, 2.0, "Middle East"),
    CityLocation("Baghdad", "Iraq", 33.3152, 44.3661, 3.0, "Middle East"),
    CityLocation("Basra", "Iraq", 30.5085, 47.7804, 3.0, "Middle East"),
    CityLocation("Mosul", "Iraq", 36.3400, 43.1300, 3.0, "Middle East"),
    CityLocation("Erbil", "Iraq", 36.1901, 44.0091, 3.0, "Middle East"),
    CityLocation("Najaf", "Iraq", 32.0259, 44.3463, 3.0, "Middle East"),
    CityLocation("Karbala", "Iraq", 32.6160, 44.0249, 3.0, "Middle East"),
    CityLocation("Sulaymaniyah", "Iraq", 35.5558, 45.4351, 3.0, "Middle East"),
    CityLocation("Damascus", "Syria", 33.5138, 36.2765, 3.0, "Middle East"),
    CityLocation("Aleppo", "Syria", 36.2021, 37.1343, 3.0, "Middle East"),
    CityLocation("Homs", "Syria", 34.7324, 36.7137, 3.0, "Middle East"),
    CityLocation("Jerusalem", "Palestine", 31.7683, 35.2137, 2.0, "Middle East"),
    CityLocation("Gaza", "Palestine", 31.5017, 34.4668, 2.0, "Middle East"),
    CityLocation("Ramallah", "Palestine", 31.9038, 35.2034, 2.0, "Middle East"),
    CityLocation("Hebron", "Palestine", 31.5326, 35.0998, 2.0, "Middle East"),
    CityLocation("Nablus", "Palestine", 32.2227, 35.2621, 2.0, "Middle East"),
    CityLocation("Sana'a", "Yemen", 15.3694, 44.1910, 3.0, "Middle East"),
    CityLocation("Aden", "Yemen", 12.7855, 45.0187, 3.0, "Middle East"),
    CityLocation("Taiz", "Yemen", 13.5795, 44.0209, 3.0, "Middle East"),
    CityLocation("Tehran", "Iran", 35.6892, 51.3890, 3.5, "Middle East"),
    CityLocation("Mashhad", "Iran", 36.2972, 59.6067, 3.5, "Middle East"),
    CityLocation("Isfahan", "Iran", 32.6546, 51.6680, 3.5, "Middle East"),
    CityLocation("Shiraz", "Iran", 29.5918, 52.5837, 3.5, "Middle East"),
    CityLocation("Tabriz", "Iran", 38.0800, 46.2919, 3.5, "Middle East"),

    // --- AFRICA ---
    CityLocation("Cairo", "Egypt", 30.0444, 31.2357, 2.0, "Africa"),
    CityLocation("Alexandria", "Egypt", 31.2001, 29.9187, 2.0, "Africa"),
    CityLocation("Giza", "Egypt", 30.0131, 31.2089, 2.0, "Africa"),
    CityLocation("Port Said", "Egypt", 31.2653, 32.3019, 2.0, "Africa"),
    CityLocation("Luxor", "Egypt", 25.6872, 32.6396, 2.0, "Africa"),
    CityLocation("Aswan", "Egypt", 24.0889, 32.8998, 2.0, "Africa"),
    CityLocation("Casablanca", "Morocco", 33.5731, -7.5898, 1.0, "Africa"),
    CityLocation("Rabat", "Morocco", 34.0209, -6.8416, 1.0, "Africa"),
    CityLocation("Marrakech", "Morocco", 31.6295, -7.9811, 1.0, "Africa"),
    CityLocation("Fes", "Morocco", 34.0181, -5.0078, 1.0, "Africa"),
    CityLocation("Tangier", "Morocco", 35.7595, -5.8340, 1.0, "Africa"),
    CityLocation("Agadir", "Morocco", 30.4278, -9.5981, 1.0, "Africa"),
    CityLocation("Algiers", "Algeria", 36.7538, 3.0588, 1.0, "Africa"),
    CityLocation("Oran", "Algeria", 35.6987, -0.6349, 1.0, "Africa"),
    CityLocation("Constantine", "Algeria", 36.3650, 6.6147, 1.0, "Africa"),
    CityLocation("Annaba", "Algeria", 36.9000, 7.7667, 1.0, "Africa"),
    CityLocation("Tunis", "Tunisia", 36.8065, 10.1815, 1.0, "Africa"),
    CityLocation("Sfax", "Tunisia", 34.7406, 10.7603, 1.0, "Africa"),
    CityLocation("Sousse", "Tunisia", 35.8256, 10.6369, 1.0, "Africa"),
    CityLocation("Kairouan", "Tunisia", 35.6781, 10.0963, 1.0, "Africa"),
    CityLocation("Tripoli", "Libya", 32.8872, 13.1913, 2.0, "Africa"),
    CityLocation("Benghazi", "Libya", 32.1167, 20.0667, 2.0, "Africa"),
    CityLocation("Misrata", "Libya", 32.3754, 15.0925, 2.0, "Africa"),
    CityLocation("Khartoum", "Sudan", 15.5007, 32.5599, 2.0, "Africa"),
    CityLocation("Omdurman", "Sudan", 15.6500, 32.4833, 2.0, "Africa"),
    CityLocation("Port Sudan", "Sudan", 19.6175, 37.2164, 2.0, "Africa"),
    CityLocation("Nouakchott", "Mauritania", 18.0735, -15.9582, 0.0, "Africa"),
    CityLocation("Mogadishu", "Somalia", 2.0469, 45.3182, 3.0, "Africa"),
    CityLocation("Hargeisa", "Somalia", 9.5600, 44.0650, 3.0, "Africa"),
    CityLocation("Djibouti City", "Djibouti", 11.5721, 43.1456, 3.0, "Africa"),
    CityLocation("Lagos", "Nigeria", 6.5244, 3.3792, 1.0, "Africa"),
    CityLocation("Abuja", "Nigeria", 9.0765, 7.3986, 1.0, "Africa"),
    CityLocation("Kano", "Nigeria", 12.0022, 8.5920, 1.0, "Africa"),
    CityLocation("Ibadan", "Nigeria", 7.3775, 3.9470, 1.0, "Africa"),
    CityLocation("Kaduna", "Nigeria", 10.5105, 7.4165, 1.0, "Africa"),
    CityLocation("Port Harcourt", "Nigeria", 4.8156, 7.0498, 1.0, "Africa"),
    CityLocation("Maiduguri", "Nigeria", 11.8333, 13.1500, 1.0, "Africa"),
    CityLocation("Zaria", "Nigeria", 11.0855, 7.7199, 1.0, "Africa"),
    CityLocation("Ilorin", "Nigeria", 8.4966, 4.5421, 1.0, "Africa"),
    CityLocation("Jos", "Nigeria", 9.8965, 8.8583, 1.0, "Africa"),
    CityLocation("Sokoto", "Nigeria", 13.0609, 5.2476, 1.0, "Africa"),
    CityLocation("Accra", "Ghana", 5.6037, -0.1870, 0.0, "Africa"),
    CityLocation("Kumasi", "Ghana", 6.6885, -1.6244, 0.0, "Africa"),
    CityLocation("Tamale", "Ghana", 9.4008, -0.8393, 0.0, "Africa"),
    CityLocation("Dakar", "Senegal", 14.7167, -17.4677, 0.0, "Africa"),
    CityLocation("Touba", "Senegal", 14.8667, -15.8833, 0.0, "Africa"),
    CityLocation("Thies", "Senegal", 14.7833, -16.9167, 0.0, "Africa"),
    CityLocation("Bamako", "Mali", 12.6392, -8.0029, 0.0, "Africa"),
    CityLocation("Niamey", "Niger", 13.5116, 2.1254, 1.0, "Africa"),
    CityLocation("Maradi", "Niger", 13.5000, 7.1000, 1.0, "Africa"),
    CityLocation("Zinder", "Niger", 13.8072, 8.9883, 1.0, "Africa"),
    CityLocation("N'Djamena", "Chad", 12.1348, 15.0557, 1.0, "Africa"),
    CityLocation("Conakry", "Guinea", 9.6412, -13.5784, 0.0, "Africa"),
    CityLocation("Freetown", "Sierra Leone", 8.4844, -13.2344, 0.0, "Africa"),
    CityLocation("Banjul", "Gambia", 13.4549, -16.5790, 0.0, "Africa"),
    CityLocation("Nairobi", "Kenya", -1.2921, 36.8219, 3.0, "Africa"),
    CityLocation("Mombasa", "Kenya", -4.0435, 39.6682, 3.0, "Africa"),
    CityLocation("Dar es Salaam", "Tanzania", -6.7924, 39.2083, 3.0, "Africa"),
    CityLocation("Zanzibar City", "Tanzania", -6.1659, 39.2026, 3.0, "Africa"),
    CityLocation("Dodoma", "Tanzania", -6.1630, 35.7516, 3.0, "Africa"),
    CityLocation("Kampala", "Uganda", 0.3476, 32.5825, 3.0, "Africa"),
    CityLocation("Kigali", "Rwanda", -1.9706, 30.1044, 2.0, "Africa"),
    CityLocation("Addis Ababa", "Ethiopia", 9.0300, 38.7400, 3.0, "Africa"),
    CityLocation("Dire Dawa", "Ethiopia", 9.6009, 41.8661, 3.0, "Africa"),
    CityLocation("Johannesburg", "South Africa", -26.2041, 28.0473, 2.0, "Africa"),
    CityLocation("Cape Town", "South Africa", -33.9249, 18.4241, 2.0, "Africa"),
    CityLocation("Durban", "South Africa", -29.8587, 31.0218, 2.0, "Africa"),

    // --- ASIA (South, Southeast, East & Central) ---
    CityLocation("Karachi", "Pakistan", 24.8607, 67.0011, 5.0, "Asia"),
    CityLocation("Lahore", "Pakistan", 31.5497, 74.3436, 5.0, "Asia"),
    CityLocation("Islamabad", "Pakistan", 33.6844, 73.0479, 5.0, "Asia"),
    CityLocation("Rawalpindi", "Pakistan", 33.5651, 73.0169, 5.0, "Asia"),
    CityLocation("Faisalabad", "Pakistan", 31.4504, 73.1350, 5.0, "Asia"),
    CityLocation("Peshawar", "Pakistan", 34.0151, 71.5249, 5.0, "Asia"),
    CityLocation("Multan", "Pakistan", 30.1575, 71.5249, 5.0, "Asia"),
    CityLocation("Quetta", "Pakistan", 30.1798, 66.9750, 5.0, "Asia"),
    CityLocation("Sialkot", "Pakistan", 32.4945, 74.5229, 5.0, "Asia"),
    CityLocation("Gujranwala", "Pakistan", 32.1877, 74.1945, 5.0, "Asia"),
    CityLocation("Dhaka", "Bangladesh", 23.8103, 90.4125, 6.0, "Asia"),
    CityLocation("Chittagong", "Bangladesh", 22.3569, 91.7832, 6.0, "Asia"),
    CityLocation("Sylhet", "Bangladesh", 24.8949, 91.8687, 6.0, "Asia"),
    CityLocation("Khulna", "Bangladesh", 22.8456, 89.5403, 6.0, "Asia"),
    CityLocation("Rajshahi", "Bangladesh", 24.3636, 88.6241, 6.0, "Asia"),
    CityLocation("New Delhi", "India", 28.6139, 77.2090, 5.5, "Asia"),
    CityLocation("Mumbai", "India", 19.0760, 72.8777, 5.5, "Asia"),
    CityLocation("Bengaluru", "India", 12.9716, 77.5946, 5.5, "Asia"),
    CityLocation("Hyderabad", "India", 17.3850, 78.4867, 5.5, "Asia"),
    CityLocation("Chennai", "India", 13.0827, 80.2707, 5.5, "Asia"),
    CityLocation("Kolkata", "India", 22.5726, 88.3639, 5.5, "Asia"),
    CityLocation("Ahmedabad", "India", 23.0225, 72.5714, 5.5, "Asia"),
    CityLocation("Lucknow", "India", 26.8467, 80.9462, 5.5, "Asia"),
    CityLocation("Srinagar", "India", 34.0837, 74.7973, 5.5, "Asia"),
    CityLocation("Kozhikode", "India", 11.2588, 75.7804, 5.5, "Asia"),
    CityLocation("Kabul", "Afghanistan", 34.5553, 69.2075, 4.5, "Asia"),
    CityLocation("Kandahar", "Afghanistan", 31.6289, 65.7372, 4.5, "Asia"),
    CityLocation("Herat", "Afghanistan", 34.3529, 62.2040, 4.5, "Asia"),
    CityLocation("Mazar-i-Sharif", "Afghanistan", 36.7090, 67.1109, 4.5, "Asia"),
    CityLocation("Colombo", "Sri Lanka", 6.9271, 79.8612, 5.5, "Asia"),
    CityLocation("Malé", "Maldives", 4.1755, 73.5093, 5.0, "Asia"),
    CityLocation("Jakarta", "Indonesia", -6.2088, 106.8456, 7.0, "Asia"),
    CityLocation("Surabaya", "Indonesia", -7.2575, 112.7521, 7.0, "Asia"),
    CityLocation("Bandung", "Indonesia", -6.9175, 107.6191, 7.0, "Asia"),
    CityLocation("Medan", "Indonesia", 3.5952, 98.6722, 7.0, "Asia"),
    CityLocation("Semarang", "Indonesia", -6.9667, 110.4167, 7.0, "Asia"),
    CityLocation("Makassar", "Indonesia", -5.1477, 119.4327, 8.0, "Asia"),
    CityLocation("Banda Aceh", "Indonesia", 5.5483, 95.3238, 7.0, "Asia"),
    CityLocation("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, 8.0, "Asia"),
    CityLocation("George Town", "Malaysia", 5.4164, 100.3327, 8.0, "Asia"),
    CityLocation("Johor Bahru", "Malaysia", 1.4927, 103.7414, 8.0, "Asia"),
    CityLocation("Kota Kinabalu", "Malaysia", 5.9804, 116.0735, 8.0, "Asia"),
    CityLocation("Kuching", "Malaysia", 1.5533, 110.3592, 8.0, "Asia"),
    CityLocation("Kota Bharu", "Malaysia", 6.1254, 102.2381, 8.0, "Asia"),
    CityLocation("Singapore", "Singapore", 1.3521, 103.8198, 8.0, "Asia"),
    CityLocation("Bandar Seri Begawan", "Brunei", 4.9031, 114.9398, 8.0, "Asia"),
    CityLocation("Bangkok", "Thailand", 13.7563, 100.5018, 7.0, "Asia"),
    CityLocation("Pattani", "Thailand", 6.8672, 101.2501, 7.0, "Asia"),
    CityLocation("Hat Yai", "Thailand", 7.0084, 100.4767, 7.0, "Asia"),
    CityLocation("Manila", "Philippines", 14.5995, 120.9842, 8.0, "Asia"),
    CityLocation("Cotabato City", "Philippines", 7.2236, 124.2464, 8.0, "Asia"),
    CityLocation("Zamboanga City", "Philippines", 6.9214, 122.0790, 8.0, "Asia"),
    CityLocation("Tashkent", "Uzbekistan", 41.2995, 69.2401, 5.0, "Asia"),
    CityLocation("Samarkand", "Uzbekistan", 39.6270, 66.9750, 5.0, "Asia"),
    CityLocation("Bukhara", "Uzbekistan", 39.7681, 64.4556, 5.0, "Asia"),
    CityLocation("Almaty", "Kazakhstan", 43.2220, 76.8512, 5.0, "Asia"),
    CityLocation("Astana", "Kazakhstan", 51.1694, 71.4491, 5.0, "Asia"),
    CityLocation("Shymkent", "Kazakhstan", 42.3417, 69.5901, 5.0, "Asia"),
    CityLocation("Bishkek", "Kyrgyzstan", 42.8746, 74.5698, 6.0, "Asia"),
    CityLocation("Osh", "Kyrgyzstan", 40.5140, 72.8161, 6.0, "Asia"),
    CityLocation("Dushanbe", "Tajikistan", 38.5598, 68.7870, 5.0, "Asia"),
    CityLocation("Ashgabat", "Turkmenistan", 37.9601, 58.3261, 5.0, "Asia"),
    CityLocation("Baku", "Azerbaijan", 40.4093, 49.8671, 4.0, "Asia"),
    CityLocation("Beijing", "China", 39.9042, 116.4074, 8.0, "Asia"),
    CityLocation("Shanghai", "China", 31.2304, 121.4737, 8.0, "Asia"),
    CityLocation("Guangzhou", "China", 23.1291, 113.2644, 8.0, "Asia"),
    CityLocation("Urumqi", "China", 43.8256, 87.6168, 8.0, "Asia"),
    CityLocation("Kashgar", "China", 39.4677, 75.9898, 8.0, "Asia"),
    CityLocation("Xi'an", "China", 34.3416, 108.9398, 8.0, "Asia"),
    CityLocation("Tokyo", "Japan", 35.6762, 139.6503, 9.0, "Asia"),
    CityLocation("Osaka", "Japan", 34.6937, 135.5023, 9.0, "Asia"),
    CityLocation("Seoul", "South Korea", 37.5665, 126.9780, 9.0, "Asia"),
    CityLocation("Hong Kong", "Hong Kong", 22.3193, 114.1694, 8.0, "Asia"),
    CityLocation("Taipei", "Taiwan", 25.0330, 121.5654, 8.0, "Asia"),

    // --- EUROPE ---
    CityLocation("Istanbul", "Turkey", 41.0082, 28.9784, 3.0, "Europe"),
    CityLocation("Ankara", "Turkey", 39.9334, 32.8597, 3.0, "Europe"),
    CityLocation("Izmir", "Turkey", 38.4237, 27.1428, 3.0, "Europe"),
    CityLocation("Bursa", "Turkey", 40.1885, 29.0610, 3.0, "Europe"),
    CityLocation("Antalya", "Turkey", 36.8969, 30.7133, 3.0, "Europe"),
    CityLocation("Konya", "Turkey", 37.8746, 32.4932, 3.0, "Europe"),
    CityLocation("Gaziantep", "Turkey", 37.0662, 37.3833, 3.0, "Europe"),
    CityLocation("Sanliurfa", "Turkey", 37.1674, 38.7955, 3.0, "Europe"),
    CityLocation("London", "United Kingdom", 51.5074, -0.1278, 0.0, "Europe"),
    CityLocation("Birmingham", "United Kingdom", 52.4862, -1.8904, 0.0, "Europe"),
    CityLocation("Manchester", "United Kingdom", 53.4808, -2.2426, 0.0, "Europe"),
    CityLocation("Leeds", "United Kingdom", 53.8008, -1.5491, 0.0, "Europe"),
    CityLocation("Glasgow", "United Kingdom", 55.8642, -4.2518, 0.0, "Europe"),
    CityLocation("Bradford", "United Kingdom", 53.7960, -1.7594, 0.0, "Europe"),
    CityLocation("Paris", "France", 48.8566, 2.3522, 1.0, "Europe"),
    CityLocation("Marseille", "France", 43.2965, 5.3698, 1.0, "Europe"),
    CityLocation("Lyon", "France", 45.7640, 4.8357, 1.0, "Europe"),
    CityLocation("Toulouse", "France", 43.6047, 1.4442, 1.0, "Europe"),
    CityLocation("Lille", "France", 50.6292, 3.0573, 1.0, "Europe"),
    CityLocation("Berlin", "Germany", 52.5200, 13.4050, 1.0, "Europe"),
    CityLocation("Frankfurt", "Germany", 50.1109, 8.6821, 1.0, "Europe"),
    CityLocation("Munich", "Germany", 48.1351, 11.5820, 1.0, "Europe"),
    CityLocation("Cologne", "Germany", 50.9375, 6.9603, 1.0, "Europe"),
    CityLocation("Hamburg", "Germany", 53.5511, 9.9937, 1.0, "Europe"),
    CityLocation("Düsseldorf", "Germany", 51.2277, 6.7735, 1.0, "Europe"),
    CityLocation("Stuttgart", "Germany", 48.7758, 9.1829, 1.0, "Europe"),
    CityLocation("Amsterdam", "Netherlands", 52.3676, 4.9041, 1.0, "Europe"),
    CityLocation("Rotterdam", "Netherlands", 51.9244, 4.4777, 1.0, "Europe"),
    CityLocation("The Hague", "Netherlands", 52.0705, 4.3007, 1.0, "Europe"),
    CityLocation("Brussels", "Belgium", 50.8503, 4.3517, 1.0, "Europe"),
    CityLocation("Antwerp", "Belgium", 51.2194, 4.4025, 1.0, "Europe"),
    CityLocation("Madrid", "Spain", 40.4168, -3.7038, 1.0, "Europe"),
    CityLocation("Barcelona", "Spain", 41.3879, 2.1699, 1.0, "Europe"),
    CityLocation("Cordoba", "Spain", 37.8882, -4.7794, 1.0, "Europe"),
    CityLocation("Granada", "Spain", 37.1773, -3.5986, 1.0, "Europe"),
    CityLocation("Rome", "Italy", 41.9028, 12.4964, 1.0, "Europe"),
    CityLocation("Milan", "Italy", 45.4642, 9.1900, 1.0, "Europe"),
    CityLocation("Vienna", "Austria", 48.2082, 16.3738, 1.0, "Europe"),
    CityLocation("Zurich", "Switzerland", 47.3769, 8.5417, 1.0, "Europe"),
    CityLocation("Geneva", "Switzerland", 46.2044, 6.1432, 1.0, "Europe"),
    CityLocation("Stockholm", "Sweden", 59.3293, 18.0686, 1.0, "Europe"),
    CityLocation("Malmö", "Sweden", 55.6050, 13.0038, 1.0, "Europe"),
    CityLocation("Oslo", "Norway", 59.9139, 10.7522, 1.0, "Europe"),
    CityLocation("Copenhagen", "Denmark", 55.6761, 12.5683, 1.0, "Europe"),
    CityLocation("Helsinki", "Finland", 60.1699, 24.9384, 2.0, "Europe"),
    CityLocation("Dublin", "Ireland", 53.3498, -6.2603, 0.0, "Europe"),
    CityLocation("Sarajevo", "Bosnia and Herzegovina", 43.8563, 18.4131, 1.0, "Europe"),
    CityLocation("Mostar", "Bosnia and Herzegovina", 43.3438, 17.8078, 1.0, "Europe"),
    CityLocation("Tuzla", "Bosnia and Herzegovina", 44.5384, 18.6671, 1.0, "Europe"),
    CityLocation("Pristina", "Kosovo", 42.6629, 21.1655, 1.0, "Europe"),
    CityLocation("Prizren", "Kosovo", 42.2139, 20.7397, 1.0, "Europe"),
    CityLocation("Tirana", "Albania", 41.3275, 19.8187, 1.0, "Europe"),
    CityLocation("Skopje", "North Macedonia", 42.0000, 21.4333, 1.0, "Europe"),
    CityLocation("Athens", "Greece", 37.9838, 23.7275, 2.0, "Europe"),
    CityLocation("Moscow", "Russia", 55.7558, 37.6173, 3.0, "Europe"),
    CityLocation("Kazan", "Russia", 55.8304, 49.0661, 3.0, "Europe"),
    CityLocation("Ufa", "Russia", 54.7388, 55.9721, 5.0, "Europe"),
    CityLocation("Makhachkala", "Russia", 42.9849, 47.5047, 3.0, "Europe"),
    CityLocation("Grozny", "Russia", 43.3169, 45.6985, 3.0, "Europe"),
    CityLocation("Warsaw", "Poland", 52.2297, 21.0122, 1.0, "Europe"),
    CityLocation("Kyiv", "Ukraine", 50.4501, 30.5234, 2.0, "Europe"),

    // --- AMERICAS ---
    CityLocation("New York", "United States", 40.7128, -74.0060, -5.0, "Americas"),
    CityLocation("Los Angeles", "United States", 34.0522, -118.2437, -8.0, "Americas"),
    CityLocation("Chicago", "United States", 41.8781, -87.6298, -6.0, "Americas"),
    CityLocation("Houston", "United States", 29.7604, -95.3698, -6.0, "Americas"),
    CityLocation("Dallas", "United States", 32.7767, -96.7970, -6.0, "Americas"),
    CityLocation("Dearborn", "United States", 42.3223, -83.1763, -5.0, "Americas"),
    CityLocation("Detroit", "United States", 42.3314, -83.0458, -5.0, "Americas"),
    CityLocation("Philadelphia", "United States", 39.9526, -75.1652, -5.0, "Americas"),
    CityLocation("Washington DC", "United States", 38.9072, -77.0369, -5.0, "Americas"),
    CityLocation("San Francisco", "United States", 37.7749, -122.4194, -8.0, "Americas"),
    CityLocation("Seattle", "United States", 47.6062, -122.3321, -8.0, "Americas"),
    CityLocation("Atlanta", "United States", 33.7490, -84.3880, -5.0, "Americas"),
    CityLocation("Miami", "United States", 25.7617, -80.1918, -5.0, "Americas"),
    CityLocation("Minneapolis", "United States", 44.9778, -93.2650, -6.0, "Americas"),
    CityLocation("Boston", "United States", 42.3601, -71.0589, -5.0, "Americas"),
    CityLocation("Toronto", "Canada", 43.6532, -79.3832, -5.0, "Americas"),
    CityLocation("Montreal", "Canada", 45.5017, -73.5673, -5.0, "Americas"),
    CityLocation("Vancouver", "Canada", 49.2827, -123.1207, -8.0, "Americas"),
    CityLocation("Calgary", "Canada", 51.0447, -114.0719, -7.0, "Americas"),
    CityLocation("Ottawa", "Canada", 45.4215, -75.6972, -5.0, "Americas"),
    CityLocation("Edmonton", "Canada", 53.5461, -113.4938, -7.0, "Americas"),
    CityLocation("Mexico City", "Mexico", 19.4326, -99.1332, -6.0, "Americas"),
    CityLocation("Guadalajara", "Mexico", 20.6597, -103.3496, -6.0, "Americas"),
    CityLocation("São Paulo", "Brazil", -23.5505, -46.6333, -3.0, "Americas"),
    CityLocation("Rio de Janeiro", "Brazil", -22.9068, -43.1729, -3.0, "Americas"),
    CityLocation("Foz do Iguaçu", "Brazil", -25.5163, -54.5854, -3.0, "Americas"),
    CityLocation("Buenos Aires", "Argentina", -34.6037, -58.3816, -3.0, "Americas"),
    CityLocation("Santiago", "Chile", -33.4489, -70.6693, -3.0, "Americas"),
    CityLocation("Bogota", "Colombia", 4.7110, -74.0721, -5.0, "Americas"),
    CityLocation("Lima", "Peru", -12.0464, -77.0428, -5.0, "Americas"),
    CityLocation("Georgetown", "Guyana", 6.8013, -58.1551, -4.0, "Americas"),
    CityLocation("Paramaribo", "Suriname", 5.8520, -55.2038, -3.0, "Americas"),
    CityLocation("Port of Spain", "Trinidad and Tobago", 10.6549, -61.5019, -4.0, "Americas"),

    // --- OCEANIA ---
    CityLocation("Sydney", "Australia", -33.8688, 151.2093, 10.0, "Oceania"),
    CityLocation("Melbourne", "Australia", -37.8136, 144.9631, 10.0, "Oceania"),
    CityLocation("Brisbane", "Australia", -27.4698, 153.0251, 10.0, "Oceania"),
    CityLocation("Perth", "Australia", -31.9505, 115.8605, 8.0, "Oceania"),
    CityLocation("Adelaide", "Australia", -34.9285, 138.6007, 9.5, "Oceania"),
    CityLocation("Canberra", "Australia", -35.2809, 149.1300, 10.0, "Oceania"),
    CityLocation("Darwin", "Australia", -12.4634, 130.8456, 9.5, "Oceania"),
    CityLocation("Auckland", "New Zealand", -36.8485, 174.7633, 12.0, "Oceania"),
    CityLocation("Wellington", "New Zealand", -41.2865, 174.7762, 12.0, "Oceania"),
    CityLocation("Christchurch", "New Zealand", -43.5321, 172.6362, 12.0, "Oceania"),
    CityLocation("Suva", "Fiji", -18.1248, 178.4501, 12.0, "Oceania")
  )

  /**
   * Fast search across city name, country, and region.
   */
  fun searchCities(query: String, regionFilter: String = "All"): List<CityLocation> {
    val q = query.trim().lowercase()
    val baseList = if (regionFilter == "All") ALL_CITIES else ALL_CITIES.filter { it.region.equals(regionFilter, ignoreCase = true) }
    if (q.isBlank()) return baseList

    return baseList.filter {
      it.name.lowercase().contains(q) ||
      it.country.lowercase().contains(q) ||
      it.region.lowercase().contains(q)
    }
  }

  fun getCityByName(name: String): CityLocation {
    return ALL_CITIES.firstOrNull { it.name.equals(name, ignoreCase = true) }
      ?: ALL_CITIES[0] // Default Mecca
  }

  /**
   * Find the closest city in the directory using the Haversine distance formula.
   */
  fun findNearestCity(lat: Double, lng: Double): CityLocation {
    var closest = ALL_CITIES[0]
    var minDistanceKm = Double.MAX_VALUE

    for (city in ALL_CITIES) {
      val distance = calculateDistanceKm(lat, lng, city.latitude, city.longitude)
      if (distance < minDistanceKm) {
        minDistanceKm = distance
        closest = city
      }
    }
    return closest
  }

  /**
   * Calculates great-circle distance between two coordinates in kilometers.
   */
  private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Earth radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
  }
}
