package com.holubek.trashhunter
import java.util.*

/**
 * Trieda na zobrazenie vhodného formátu dátumu a času
 */
class DateFormat {
    companion object {

        /**
         * Vráti upravený formát dátumu z objektu Date
         * @param date dátum na zobrazenie
         */
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

        /**
         * Vráti upravený formát dátumu z objektu Calendar
         * @param calendar dátum na zobrazenie
         */
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

        /**
         * Vráti upravený formát dátumu a času
         * @param date dátum na zobrazenie
         */
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
                hoursString = (hours).toString()
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

        /**
         * Vráti upravený formát času
         * @param date dátum na zobrazenie času
         */
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