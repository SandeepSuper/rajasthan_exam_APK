package com.rajasthanexams.data

import androidx.compose.ui.graphics.Color
import com.rajasthanexams.ui.theme.RoyalBlue
import com.rajasthanexams.ui.theme.Saffron
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.graphics.vector.ImageVector

enum class TestType {
    MOCK, TOPIC, FULL, PYQ
}

data class Category(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val testsAvailable: Int
)

data class Test(
    val id: String,
    val title: String,
    val category: String,
    val questions: Int,
    val time: Int,
    val attempts: String,
    val rating: Double,
    val isLive: Boolean,
    val type: TestType = TestType.MOCK
)

    data class Question(
        val id: String,
        val questionEn: String,
        val questionHi: String,
        val optionsEn: List<String>,
        val optionsHi: List<String>,
        val correctOptionIndex: Int, // 0-3
        val solutionEn: String,
        val solutionHi: String
    )

    data class Promotion(
        val id: String,
        val title: String,
        val subtitle: String,
        val discount: String,
        val colorStart: Color,
        val colorEnd: Color
    )



    data class TestResult(
        val id: String,
        val testTitle: String,
        val score: Int,
        val totalMarks: Int,
        val date: String,
        val isPass: Boolean
    )

    enum class NotificationType {
        UPDATE, OFFER, LIVE_TEST
    }

    data class Notification(
        val id: String,
        val title: String,
        val description: String,
        val type: NotificationType,
        val timeAgo: String
    )

    object MockData {
        val testHistory = listOf(
            TestResult("1", "Patwari Mock Test 1", 240, 300, "12 Jan", true),
            TestResult("2", "RAS Prelims History", 85, 150, "10 Jan", false),
            TestResult("3", "REET Level 2 Psychology", 28, 30, "08 Jan", true),
            TestResult("4", "CET General Science", 45, 50, "05 Jan", true)
        )

        val notifications = listOf(
            Notification("1", "New Patwari Mock Test Live!", "Full syllabus mock test #5 is now available. Attempt now to check your rank.", NotificationType.LIVE_TEST, "2 hrs ago"),
            Notification("2", "50% OFF on RAS Course", "Limited time offer on our comprehensive RAS Prelims 2024 course. Use code RAS50.", NotificationType.OFFER, "5 hrs ago"),
            Notification("3", "App Update Available", "We have improved performance and fixed bugs in the quiz engine. Update now.", NotificationType.UPDATE, "1 day ago"),
            Notification("4", "REET Syllabus Changed", "As per the new guidelines, minor changes in Level 2 Science syllabus. Check details.", NotificationType.UPDATE, "2 days ago"),
            Notification("5", "Sunday Live Marathon", "Join us for a 4-hour live revision session this Sunday at 10 AM.", NotificationType.LIVE_TEST, "3 days ago")
        )

        val promotions = listOf(
            Promotion("1", "Patwari Mega Pack", "Full Course + 50 Mock Tests", "50% OFF", Color(0xFF6A11CB), Color(0xFF2575FC)),
            Promotion("2", "RAS Prelims Crash Course", "Live Classes & Notes", "New", Color(0xFFFF512F), Color(0xFFDD2476)),
            Promotion("3", "REET Level 2 Science", "Complete Syllabus", "30% OFF", Color(0xFF11998e), Color(0xFF38ef7d))
        )

        val categories = listOf(
            Category("1", "Rajasthan Patwari", Icons.Default.Description, 25),
            Category("2", "Rajasthan SI", Icons.Default.Security, 18),
            Category("3", "CET (Graduate)", Icons.Default.School, 12),
            Category("4", "RAS Prelims", Icons.Default.AccountBalance, 10),
            Category("5", "REET", Icons.Default.Edit, 30)
        )

        val popularTests = listOf(
            Test("101", "Patwari Full Mock Test 1", "Patwari", 150, 180, "15k+", 4.8, true, TestType.FULL),
            Test("102", "RAS General Knowledge Daily", "RAS", 20, 15, "8.5k+", 4.5, false, TestType.TOPIC),
            Test("103", "REET Child Development", "REET", 30, 30, "22k+", 4.9, false, TestType.TOPIC),
            Test("104", "Rajasthan SI Hindi Special", "SI", 100, 120, "10k+", 4.7, true, TestType.MOCK),
            Test("105", "CET General Science", "CET", 50, 45, "5k+", 4.6, false, TestType.TOPIC),
            Test("106", "Patwari 2021 Paper (Shift 1)", "Patwari", 150, 180, "25k+", 4.9, false, TestType.PYQ)
        )
        
        val sampleQuestions = listOf(
            Question(
                "q1", 
                "Where is the Hawa Mahal located?", 
                "हवा महल कहां स्थित है?",
                listOf("Udaipur", "Jodhpur", "Jaipur", "Bikaner"),
                listOf("उदयपुर", "जोधपुर", "जयपुर", "बीकानेर"),
                2, // Jaipur
                "Hawa Mahal is a palace in Jaipur, India. Built from red and pink sandstone, it is on the edge of the City Palace.",
                "हवा महल जयपुर, भारत में एक महल है। लाल और गुलाबी बलुआ पत्थर से निर्मित, यह सिटी पैलेस के किनारे पर स्थित है।"
            ),
            Question(
                "q2", 
                "Who was the first Chief Minister of Rajasthan?", 
                "राजस्थान के प्रथम मुख्यमंत्री कौन थे?",
                listOf("Mohan Lal Sukhadia", "Heera Lal Shastri", "Jai Narayan Vyas", "Vasundhara Raje"),
                listOf("मोहन लाल सुखाड़िया", "हीरा लाल शास्त्री", "जय नारायण व्यास", "वसुंधरा राजे"),
                1, // Heera Lal Shastri
                "Heera Lal Shastri was the first Chief Minister of Rajasthan. He took office on April 7, 1949.",
                "हीरा लाल शास्त्री राजस्थान के पहले मुख्यमंत्री थे। उन्होंने 7 अप्रैल, 1949 को पदभार ग्रहण किया।"
            ),
            Question(
                "q3", 
                "Which river is known as the 'Lifeline of Rajasthan'?", 
                "किस नदी को 'राजस्थान की जीवन रेखा' कहा जाता है?",
                listOf("Chambal", "Luni", "Mahi", "Indira Gandhi Canal"),
                listOf("चंबल", "लूनी", "माही", "इंदिरा गांधी नहर"),
                3, // Indira Gandhi Canal
                "The Indira Gandhi Canal is the longest canal of India. It starts from the Harike Barrage at Harike and terminates in irrigation facilities in the Thar Desert.",
                "इंदिरा गांधी नहर भारत की सबसे लंबी नहर है। यह हरिके बैराज से शुरू होती है और थार रेगिस्तान में सिंचाई सुविधाओं में समाप्त होती है।"
            ),
            Question(
                "q4", 
                "The famous Pushkar Fair is held in which month?", 
                "प्रसिद्ध पुष्कर मेला किस महीने में आयोजित किया जाता है?",
                listOf("November", "October", "January", "March"),
                listOf("नवंबर", "अक्टूबर", "जनवरी", "मार्च"),
                0, // November
                "The Pushkar Fair is an annual multi-day livestock fair and cultural fête held in the town of Pushkar (Rajasthan). It typically falls in November.",
                "पुष्कर मेला एक वार्षिक बहु-दिवसीय पशु मेला और सांस्कृतिक उत्सव है जो पुष्कर (राजस्थान) शहर में आयोजित किया जाता है। यह आमतौर पर नवंबर में आता है।"
            ),
            Question(
                "q5", 
                "Which distinct style of painting is associated with Kishangarh?", 
                "किशनगढ़ शैली किस कला से संबंधित है?",
                listOf("Bani Thani", "Pichwai", "Phad", "Thewa"),
                listOf("बनी-ठनी", "पिछवाई", "फड़", "थेवा"),
                0, // Bani Thani
                "Kishangarh painting is a school of Rajasthan miniature painting arising in the 18th century. It is best known for 'Bani Thani'.",
                "किशनगढ़ पेंटिंग 18वीं शताब्दी में उत्पन्न राजस्थान लघु पेंटिंग का एक स्कूल है। इसे 'बनी-ठनी' के लिए सबसे ज्यादा जाना जाता है।"
            )
        )

        val bookmarkedQuestionIds = mutableSetOf<String>()
    }
