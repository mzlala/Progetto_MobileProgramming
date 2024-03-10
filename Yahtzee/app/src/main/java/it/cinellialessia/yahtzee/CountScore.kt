package it.cinellialessia.yahtzee

class CountScore {

    fun point(dice: Int, myDice: ArrayList<Int>): Int { //dice is a Int da 1 a 13 representing the square to be played
        if (dice in 1..6) {
            return number(dice,myDice)
        } else {
            when(dice){
                7 -> return threeX(myDice)
                8 -> return fourX(myDice)
                9 -> return full(myDice)
                10 -> return lowScale(myDice)
                11 -> return bigScale(myDice)
                12 -> return yahtzee(myDice)
                13 -> return chance(myDice)
            }
            return 0
        }
    }

    //Return the score of the box in the first column
    private fun number(dice: Int, myDice: ArrayList<Int>): Int {
        var counter = 0
        for (i in myDice) {
            if (dice == i) {
                counter += dice
            }
        }
        return counter
    }

    //Return 50 if a Yahtzee was obtained
    private fun yahtzee(myDice: ArrayList<Int>) : Int {
        for(i in 0..3){
            if(myDice[i] != myDice[i+1]){
                return 0
            }
        }
        return 50
    }

    //Return the score obtained whit chance
    private fun chance(myDice: ArrayList<Int>) : Int {
        var count = 0
        for(i in myDice){
            count += i
        }
        return count
    }

    //Return 40 if a bigScale was obtained
    private fun bigScale(myDice: ArrayList<Int>): Int {
        val foundDice = arrayListOf(0,0,0,0,0,0)
        for(i in myDice){
            foundDice[i-1] += 1
        }

        if(foundDice[0] == 0){ //2-3-4-5-6
            for(i in 1..5){
                if(foundDice[i] != 1){
                    return 0
                }
            }
            return 40
        } else if(foundDice[5] == 0){ //1-2-3-4-5
            for(i in 0..4){
                if(foundDice[i] != 1){
                    return 0
                }
            }
            return 40
        }
        return 0
    }

    //Return 30 if a lowScale was obtained
    private fun lowScale(myDice: ArrayList<Int>): Int  {
        val foundDice = arrayListOf(0,0,0,0,0,0)
        var count1 = 0
        var count2 = 0
        var count3 = 0

        if(bigScale(myDice) == 40){
            return 30
        }
        for(i in myDice){
            foundDice[i-1] += 1
        }

        for(i in 0..3){ //1-2-3-4
            if(foundDice[i] >= 1){
                count1 += 1
            }
        }
        for(i in 1..4){ //2-3-4-5
            if(foundDice[i] >= 1){
                count2 += 1
            }
        }
        for(i in 2..5){ //3-4-5-6
            if(foundDice[i] >= 1){
                count3 += 1
            }
        }

        if(count1 == 4 || count2 == 4 || count3 == 4){
            return 30
        }

        return 0
    }

    //Return 25 if a full was obtained
    private fun full(myDice: ArrayList<Int>): Int  {
        val foundDice = arrayListOf(0,0,0,0,0,0)
        val full = mutableListOf(0,0)

        for(i in myDice){
            foundDice[i-1] += 1
        }

        for(i in foundDice){
            if(i == 3){
                full[0] = 1
            } else if(i == 2){
                full[1] = 1
            }
        }

        if(full[0] == 1 && full[1] == 1) {
            return 25
        }
        return 0
    }

    //Return the score obtained whit 4x
    private fun fourX(myDice: ArrayList<Int>): Int  {
        val foundDice = arrayListOf(0,0,0,0,0,0)
        var point = 0

        for(i in myDice){
            foundDice[i-1] += 1
            point += i
        }
        for(i in foundDice){
            if(i >= 4){
                return point
            }
        }
        return 0
    }

    //Return the score obtained whit 3x
    private fun threeX(myDice: ArrayList<Int>): Int {
        val foundDice = arrayListOf(0,0,0,0,0,0)
        var point = 0

        for(i in myDice){
            foundDice[i-1] += 1
            point += i
        }

        for(i in foundDice){
           if(i >= 3){
               return point
           }
        }
        return 0
    }
}