package com.amirtemur.buyukyurishlar

/**
 * Barcha tarixiy ma'lumotlar ishonchli tarixiy manbalarga
 * (Britannica, Wikipedia va Amir Temur tarixiga oid manbalar) tayanadi.
 * Sanalar va dushman nomlari real tarixga moslashtirilgan.
 */
object Data {

    val introLines = listOf(
        "O'rta Osiyo parchalangan edi...",
        "Mo'g'ullar imperiyasi quladi, amirlar o'zaro urushardi.",
        "1336-yil, Kesh (Shahrisabz) yaqinida bir bola tug'ildi.",
        "Uning ismi Temur edi.",
        "Tarix uni 'Buyuk Sarkarda' deb ataydi."
    )

    val campaigns: List<Campaign> = listOf(
        Campaign(
            id = 0,
            title = "Prolog — Kesh",
            years = "1360-yillar",
            region = "Kesh (Shahrisabz)",
            enemy = "Mahalliy raqib amirlar",
            reason = "Yosh Temur o'z yurtida nufuz va ittifoqchilar to'playdi.",
            terrain = Terrain.TOG,
            enemyPower = 210f,
            budget = 120,
            bestStrategy = Strategy.YASHIRIN,
            headline = "Buyuk yo'l Keshdan boshlandi.",
            cutscene = listOf(
                "1360-yillar. Movarounnahr tarqoq edi.",
                "Yosh Temur Kesh atrofida tarafdorlar to'pladi.",
                "Kichik, ammo tezkor otliqlar uning kuchi edi.",
                "Birinchi sinov — o'z yurtini himoya qilish."
            ),
            info = listOf(
                "Temur 1336-yilda Kesh (hozirgi Shahrisabz) yaqinida tug'ilgan.",
                "Yoshligida kichik otliq otryadlar bilan harakat qilgan.",
                "Bu davrda u ittifoqlar tuzib, siyosiy tajriba orttirgan.",
                "Maslahat: kichik tezkor qo'shin — yashirin hujum uchun ideal."
            ),
            mapX = 0.40f, mapY = 0.55f
        ),
        Campaign(
            id = 1,
            title = "1-Jang — Movarounnahr uchun kurash",
            years = "1361–1370",
            region = "Samarqand, Kesh, Buxoro",
            enemy = "Mahalliy amirlar va mo'g'ullar",
            reason = "Tarqoq Movarounnahrni yagona hokimiyat ostida birlashtirish.",
            terrain = Terrain.OCHIQ,
            enemyPower = 385f,
            budget = 220,
            bestStrategy = Strategy.FRONTAL,
            headline = "Temur Movarounnahrni birlashtirishni boshladi.",
            cutscene = listOf(
                "Movarounnahr o'nlab amirlar o'rtasida bo'lingan edi.",
                "Temur tezkor otliq qo'shin bilan zarba berdi.",
                "1370-yilga kelib u Samarqandni qo'lga kiritdi.",
                "Samarqand uning poytaxtiga aylandi."
            ),
            info = listOf(
                "1370-yilda Temur Movarounnahrning amaldagi hukmdoriga aylandi.",
                "U Samarqandni poytaxt qilib tanladi.",
                "Dastlab Chig'atoy xonlari nomidan boshqargan.",
                "Tezkor otliq hujum uning asosiy harbiy uslubi edi."
            ),
            mapX = 0.45f, mapY = 0.50f
        ),
        Campaign(
            id = 2,
            title = "2-Jang — Xorazm yurishi",
            years = "1371–1388",
            region = "Xorazm (Urganch)",
            enemy = "Xorazm hukmdorlari (Sufiylar sulolasi)",
            reason = "Xorazm bir necha bor qo'zg'olon ko'targan strategik hudud edi.",
            terrain = Terrain.SHAHAR,
            enemyPower = 525f,
            budget = 300,
            bestStrategy = Strategy.QAMAL,
            headline = "Xorazm bir necha marta qo'zg'olon ko'tardi.",
            cutscene = listOf(
                "Xorazm boy va mustahkam qal'alarga ega edi.",
                "Temur bir necha bosqichli yurishlar uyushtirdi.",
                "Qal'alar uzoq qamal qilindi.",
                "1388-yilda Urganch nihoyat bo'ysundirildi."
            ),
            info = listOf(
                "Temur Xorazmga 1371–1388 yillar oralig'ida bir necha bor yurish qildi.",
                "Xorazm qayta-qayta qo'zg'olon ko'targani uchun kurash uzoq davom etdi.",
                "1388-yilda Urganch qattiq jazolandi.",
                "Maslahat: mustahkam shahar uchun qamal qurollari kerak."
            ),
            mapX = 0.30f, mapY = 0.40f
        ),
        Campaign(
            id = 3,
            title = "3-Jang — Oltin O'rda urushi",
            years = "1391",
            region = "Qozog'iston dashtlari (Qunduzcha daryosi)",
            enemy = "To'xtamishxon",
            reason = "To'xtamish Temurning sobiq ittifoqchisi edi, keyin unga qarshi chiqdi.",
            terrain = Terrain.DASHT,
            enemyPower = 735f,
            budget = 420,
            bestStrategy = Strategy.QANOT,
            headline = "To'xtamish Temurning eng xavfli raqiblaridan edi.",
            cutscene = listOf(
                "To'xtamish Oltin O'rdani birlashtirib, Temurga xavf soldi.",
                "Temur ulkan otliq qo'shin bilan uzoq dashtga yurdi.",
                "U armiyani qanotlarga bo'lib hujum qildi.",
                "1391-yil Qunduzcha jangida To'xtamish mag'lub bo'ldi."
            ),
            info = listOf(
                "1391-yilda Qunduzcha (Kondurcha) daryosi yonida katta jang bo'ldi.",
                "Temur uzoq dasht yurishidan so'ng To'xtamishni yengdi.",
                "Keyinroq, 1395-yilda Terek daryosida yana bir hal qiluvchi jang bo'ladi.",
                "Maslahat: dashtda otliqlar bilan qanotdan aylanish kuchli."
            ),
            mapX = 0.42f, mapY = 0.22f
        ),
        Campaign(
            id = 4,
            title = "4-Jang — Hindiston yurishi",
            years = "1398",
            region = "Dehli (Hindiston)",
            enemy = "Dehli sultonligi qo'shini",
            reason = "Boylik va shimoliy Hindistonni nazorat qilish maqsadida yurish.",
            terrain = Terrain.ISSIQ,
            enemyPower = 840f,
            budget = 480,
            bestStrategy = Strategy.MUDOFAA,
            headline = "Dehli sari buyuk yurish boshlandi.",
            cutscene = listOf(
                "Dehli qo'shinida jangovar fillar bor edi.",
                "Temur xandaq qazib, mudofaa mavqeini egalladi.",
                "Tuyalar ustiga yuk ortilib, olov yoqildi.",
                "Qo'rqqan fillar orqaga qaytib, dushmanni tor-mor qildi."
            ),
            info = listOf(
                "1398-yilda Temur Dehliga yurish qildi va shaharni egalladi.",
                "Jangovar fillarga qarshi xandaq, temir tikan va olovli tuyalardan foydalandi.",
                "Issiq iqlim va uzoq masofa qo'shin uchun sinov bo'ldi.",
                "Maslahat: fillarga qarshi mudofaa va kamonchilar samarali."
            ),
            mapX = 0.62f, mapY = 0.68f
        ),
        Campaign(
            id = 5,
            title = "5-Jang — Usmonlilar bilan urush",
            years = "1402",
            region = "Anqara (Anatoliya)",
            enemy = "Sulton Boyazid I (Yildirim)",
            reason = "Ikki buyuk imperiya o'rtasidagi hokimiyat va chegara to'qnashuvi.",
            terrain = Terrain.OCHIQ,
            enemyPower = 1140f,
            budget = 650,
            bestStrategy = Strategy.FRONTAL,
            headline = "Anqara jangi dunyo tarixini o'zgartirdi.",
            cutscene = listOf(
                "1402-yil. Anqara yaqinida ikki ulkan qo'shin to'qnashdi.",
                "Temur suv manbalarini nazorat qilib, ustunlikka erishdi.",
                "Hal qiluvchi frontal zarba berildi.",
                "Sulton Boyazid asir olindi — bu butun dunyoni hayratga soldi."
            ),
            info = listOf(
                "Anqara jangi 1402-yil 28-iyulda bo'lib o'tdi.",
                "Temur Usmonli sultoni Boyazid I ni mag'lub etib, asir oldi.",
                "Bu g'alaba Usmonlilar imperiyasini bir necha yil tanazzulga uchratdi.",
                "Maslahat: ochiq maydonda katta qo'shin bilan frontal zarba."
            ),
            mapX = 0.10f, mapY = 0.45f
        ),
        Campaign(
            id = 6,
            title = "Final — So'nggi yurish",
            years = "1405",
            region = "O'tror (Xitoy yo'nalishi)",
            enemy = "—",
            reason = "Min sulolasi Xitoyiga qarshi buyuk yurishga tayyorgarlik.",
            terrain = Terrain.DASHT,
            enemyPower = 0f,
            budget = 0,
            bestStrategy = Strategy.FRONTAL,
            headline = "Buyuk sarkarda ortida ulkan imperiya qoldirdi.",
            cutscene = listOf(
                "1404-yil qishi. Temur Xitoyga yurishni boshladi.",
                "Sovuq, qor va kasallik qo'shinni qiynadi.",
                "1405-yil fevralda O'tror shahrida Temur vafot etdi.",
                "U ortida Samarqanddan to G'arbga cho'zilgan imperiya qoldirdi."
            ),
            info = listOf(
                "Temur 1405-yil fevral oyida O'tror shahrida vafot etdi.",
                "U Min Xitoyiga qarshi yurish chog'ida kasallikdan vafot etgan.",
                "Uning maqbarasi — Samarqanddagi Go'ri Amir.",
                "Temur tarixda buyuk sarkarda va davlat arbobi sifatida qoldi."
            ),
            mapX = 0.52f, mapY = 0.30f,
            isFinal = true
        )
    )

    val endingLines = listOf(
        "Amir Temur imperiyasi Samarqanddan G'arbga cho'zildi.",
        "Movarounnahr, Xorazm, Eron, Kavkaz va undan narilari.",
        "Samarqand ilm, san'at va me'morchilik markaziga aylandi.",
        "Buyuk sarkarda ortida ulkan meros qoldirdi.",
        "TAMOM — Amir Temur: Buyuk Yurishlar"
    )
}
