package com.example.trashhunter
import java.util.*

class DateFormat {
    companion object {
        @JvmStatic
        fun getDateFormat(date: Date): String{
            val calendar = Calendar.getInstance()
            calendar.setTime(date)
            val dayInt = calendar.get(Calendar.DAY_OF_WEEK)
            val monthInt = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val year = calendar.get(Calendar.YEAR)
            var month: String = ""
            var dayOfWeek: String = ""
            when(monthInt){
                0 -> month = "Január"
                1 -> month = "Február"
                2 -> month = "Marec"
                3 -> month = "Apríl"
                4 -> month = "Máj"
                5 -> month = "Jún"
                6 -> month = "Júl"
                7 -> month = "August"
                8 -> month = "September"
                9 -> month = "Október"
                10 -> month = "November"
                11 -> month = "December"
            }

            when(dayInt){
                2 -> dayOfWeek = "Pondelok"
                3 -> dayOfWeek = "Utorok"
                4 -> dayOfWeek = "Streda"
                5 -> dayOfWeek = "Štvrtok"
                6 -> dayOfWeek = "Piatok"
                7 -> dayOfWeek = "Sobota"
                1 -> dayOfWeek = "Nedeľa"
            }
            return "$dayOfWeek, $month $day. $year"
        }
        @JvmStatic
        fun getDateFormat(calendar: Calendar): String{
            val dayInt = calendar.get(Calendar.DAY_OF_WEEK)
            val monthInt = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val year = calendar.get(Calendar.YEAR)
            var month: String = ""
            var dayOfWeek: String = ""
            when(monthInt){
                0 -> month = "Január"
                1 -> month = "Február"
                2 -> month = "Marec"
                3 -> month = "Apríl"
                4 -> month = "Máj"
                5 -> month = "Jún"
                6 -> month = "Júl"
                7 -> month = "August"
                8 -> month = "September"
                9 -> month = "Október"
                10 -> month = "November"
                11 -> month = "December"
            }

            when(dayInt){
                2 -> dayOfWeek = "Pondelok"
                3 -> dayOfWeek = "Utorok"
                4 -> dayOfWeek = "Streda"
                5 -> dayOfWeek = "Štvrtok"
                6 -> dayOfWeek = "Piatok"
                7 -> dayOfWeek = "Sobota"
                1 -> dayOfWeek = "Nedeľa"
            }
            return "$dayOfWeek, $month $day. $year"
        }
        @JvmStatic
        fun getDateTimeFormat(date: Date): String{
            val calendar = Calendar.getInstance()
            calendar.setTime(date)
            var hours = calendar.get(Calendar.HOUR_OF_DAY)
            var minutes = calendar.get(Calendar.MINUTE)

            var hoursString: String = ""
            var minutesString: String = ""
            if (hours < 10){
                hoursString = "0$hours"
            }else if(hours > 12){
                hoursString = (12 + hours).toString()
            }else{
                hoursString = hours.toString()
            }
            minutesString = if (minutes < 10){
                "0$minutes"
            }else{
                minutes.toString()
            }
            return getDateFormat(date) + " " +  ", " + hoursString + ":" + minutesString
        }

        @JvmStatic
        fun getTimeFormat(date: Date): String{
            val calendar = Calendar.getInstance()
            calendar.setTime(date)
            var hours = calendar.get(Calendar.HOUR_OF_DAY)
            var minutes = calendar.get(Calendar.MINUTE)

            return "$hours : $minutes"
        }

    }
}