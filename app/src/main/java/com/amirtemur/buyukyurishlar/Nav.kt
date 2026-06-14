package com.amirtemur.buyukyurishlar

/** Sahna oqimi uchun umumiy navigatsiya */
object Nav {
    fun startCampaign(game: Game, c: Campaign) {
        game.selectedCampaign = c
        game.setScreen(CutsceneScreen(game, c.cutscene,
            if (c.isFinal) "YAKUN" else "YO'LGA CHIQISH ▸", c.title) {
            if (c.isFinal) game.setScreen(EndingScreen(game))
            else game.setScreen(TravelScreen(game))
        })
    }

    fun nextCampaign(game: Game, current: Campaign): Campaign? =
        Data.campaigns.firstOrNull { it.id == current.id + 1 }
}
