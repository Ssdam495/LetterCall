package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val role: String, // "user" or "model"
    val text: String
)

data class LetterCurriculum(
    val letterChar: String,
    val letterName: String,
    val phonicsSound: String,
    val shapeStory: String,
    val dailyWords: String,
    val interactiveGame: String,
    val soundAction: String
)

class GeminiService(private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // 📚 المنهج التعليمي الأكاديمي الشامل لكل حرف (Montessori & Phonics Curriculum)
    private val curriculumMap: Map<String, LetterCurriculum> = mapOf(
        "أ" to LetterCurriculum(
            letterChar = "أ",
            letterName = "أَلِف",
            phonicsSound = "أَ.. أَ.. أَ",
            shapeStory = "أنا عصا طويلة وبطلة ألبس تاجاً جميلاً (الهمزة) فوق رأسي!",
            dailyWords = "أسد، أرنب، أب، أم، ألوان، أناناس",
            interactiveGame = "ابحث في غرفتك عن شيء لونه (أحمر) وأخبرني به.. أنا بانتظارك!",
            soundAction = "ازأر معي كأنك أسد شجاع: أَ.. أَ.. رررووور!"
        ),
        "ب" to LetterCurriculum(
            letterChar = "ب",
            letterName = "بَاء",
            phonicsSound = "بَ.. بَ.. بَ",
            shapeStory = "شكلي يشبه صحناً جميلاً وتحتي نقطة واحدة تشبه البطيخة اللذيذة!",
            dailyWords = "باب، بطة، بطيخ، بحر، بيت",
            interactiveGame = "أنا أفتح لك غرفتك وتدخل منها.. اسمي يبدأ بالباء.. بَ.. بَ.. من أنا؟ (باب)",
            soundAction = "قلد صوت البطة الحلوة: بَ.. بَ.. بَ.. وافتح يديك كالجناحين!"
        ),
        "ت" to LetterCurriculum(
            letterChar = "ت",
            letterName = "تَاء",
            phonicsSound = "تَ.. تَ.. تَ",
            shapeStory = "صحن جميل مبتسم يضع بداخله تفاحتين حلوتين!",
            dailyWords = "تفاح، تاج، تمساح، تمر، تلفاز",
            interactiveGame = "فاكهة لذيذة ومقرمشة لونها أحمر أو أخضر تبدأ بحرفي.. ما هي؟ (تفاح)",
            soundAction = "اضحك وأظهر أسنانك الجميلة وقل: تَ.. تَ.. تَفاح!"
        ),
        "ث" to LetterCurriculum(
            letterChar = "ث",
            letterName = "ثَاء",
            phonicsSound = "ثَ.. ثَ.. ثَ",
            shapeStory = "صحن ملكي يضع 3 نقاط مثل لآلئ التاج البراق!",
            dailyWords = "ثعلب، ثلج، ثوب، ثوم، ثعبان",
            interactiveGame = "شيء بارد جداً ينزل من السماء في الشتاء.. ثـ.. ثـ.. ما هو؟ (ثلج)",
            soundAction = "أخرج طرف لسانك الصغير بلطف وقل: ثَ.. ثَ.. ثعلب ذكي!"
        ),
        "ج" to LetterCurriculum(
            letterChar = "ج",
            letterName = "جِيم",
            phonicsSound = "جَ.. جَ.. جَ",
            shapeStory = "أنا حرف الجيم الجوعان، عندي نقطة في بطني لأني أكلت جزرة برتقالية حلوة!",
            dailyWords = "جمل، جزر، جبن، جبل، جسر",
            interactiveGame = "حيوان قوي صبور يعيش في الصحراء.. جَ.. جَ.. من هو؟ (جمل)",
            soundAction = "ضع يدك على بطنك وامسح عليها وقل: جَ.. جَ.. جزر لذيذ!"
        ),
        "ح" to LetterCurriculum(
            letterChar = "ح",
            letterName = "حَاء",
            phonicsSound = "حَ.. حَ.. حَ",
            shapeStory = "أنا حرف الحاء النظيف، غسلت رأسي بالحليب المنعش فما عندي ولا نقطة!",
            dailyWords = "حصان، حليب، حديقة، حوت، حقيبة",
            interactiveGame = "مشروب أبيض لذيذ ومفيد يجعلك بطلاً قوياً.. حَـ.. حَـ.. ما هو؟ (حليب)",
            soundAction = "صهيل الحصان السريع: حَ.. حَ.. حِصان يجري بسرعة!"
        ),
        "خ" to LetterCurriculum(
            letterChar = "خ",
            letterName = "خَاء",
            phonicsSound = "خَ.. خَ.. خَ",
            shapeStory = "أنا حرف الخاء، أضع نقطتي فوق رأسي كخيمة تحميني من المطر ومن الخروف اللطيف!",
            dailyWords = "خروف، خبز، خيار، خيمة، خاتم",
            interactiveGame = "نأكله كل يوم مع الفطور وريحته شهية جداً.. خَـ.. خَـ.. ما هو؟ (خبز)",
            soundAction = "ضع يدك فوق رأسك كأنها خيمة وقل: خَ.. خَ.. خروف!"
        ),
        "د" to LetterCurriculum(
            letterChar = "د",
            letterName = "دَال",
            phonicsSound = "دَ.. دَ.. دَ",
            shapeStory = "شكلي مثل فم الدب الصغير اللطيف وهو مفتوح يأكل العسل!",
            dailyWords = "دب، دراجة، ديك، دفتر، دلفين",
            interactiveGame = "شيء له عجلتان نركبه ونقود بسرعة في الحديقة.. دَ.. دَ.. ما هو؟ (دراجة)",
            soundAction = "صوت الديك الصباحي: كوكو.. دَ.. دَ.. ديك نشيط!"
        ),
        "ذ" to LetterCurriculum(
            letterChar = "ذ",
            letterName = "ذَال",
            phonicsSound = "ذَ.. ذَ.. ذَ",
            shapeStory = "أنا أخو الدال، لكني أحمل شمعة مضيئة فوق رأسي تنير الطريق!",
            dailyWords = "ذئب، ذرة، ذبابة، ذهب، ذيل",
            interactiveGame = "نبات أصفر حبوبه حلوة ولذيذة نشويها ونأكلها.. ذُ.. ذُ.. ما هي؟ (ذرة)",
            soundAction = "أخرج لسانك قليلاً وقل: ذَ.. ذَ.. ذئب في الغابة!"
        ),
        "ر" to LetterCurriculum(
            letterChar = "ر",
            letterName = "رَاء",
            phonicsSound = "رَ.. رَ.. رَ",
            shapeStory = "شكلي يشبه زحليقة الأطفال في الحديقة، أتزحلق بمرح وخفة!",
            dailyWords = "رمان، ريشة، رجل، رمل، ربيع",
            interactiveGame = "فاكهة حمراء مليئة بحبات الياقوت اللذيذ.. رُ.. رُ.. ما هي؟ (رمان)",
            soundAction = "حرّك لسانك وقل: ررر.. رَ.. رَ.. رمان حالي!"
        ),
        "ز" to LetterCurriculum(
            letterChar = "ز",
            letterName = "زَاي",
            phonicsSound = "زَ.. زَ.. زَ (زززز)",
            shapeStory = "زحليقة مرحة تطير فوقها فراشة صغيرة ملونة كالنقطة!",
            dailyWords = "زرافة، زهرة، زيتون، زبدة، زجاج",
            interactiveGame = "حيوان طويل القامة عنقه يصل لأعلى الشجرة.. زَ.. زَ.. من هي؟ (زرافة)",
            soundAction = "قلد طنين النحلة: زززز.. زَ.. زَ.. زهرة جميلة!"
        ),
        "س" to LetterCurriculum(
            letterChar = "س",
            letterName = "سِين",
            phonicsSound = "سَ.. سَ.. سَ (سسسس)",
            shapeStory = "أنا حرف السين الهادئ، عندي 3 أسنان صغيرة أبتسم بها دائماً!",
            dailyWords = "سمكة، سرير، سيارة، سكر، ساعة",
            interactiveGame = "شيء في غرفتك ننام عليه ونحلم أحلاماً جميلة.. سـ.. سـ.. ما هو؟ (سرير)",
            soundAction = "اسبح بيديك كالسمكة وقل: سسسس.. سَ.. سَ.. سمكة تسبح!"
        ),
        "ش" to LetterCurriculum(
            letterChar = "ش",
            letterName = "شِين",
            phonicsSound = "شَ.. شَ.. شَ",
            shapeStory = "عندي 3 أسنان جميلة وفوقي 3 نقاط تضيء كأشعة الشمس الذهبية!",
            dailyWords = "شمس، شجرة، شوكولاتة، شمعة، شاطئ",
            interactiveGame = "تنير السماء في الصباح وتدفئنا بنورها.. شَـ.. شَـ.. ما هي؟ (شمس)",
            soundAction = "افرد يديك كالشجرة الكبيرة وقل: شَ.. شَ.. شمس ساطعة!"
        ),
        "ص" to LetterCurriculum(
            letterChar = "ص",
            letterName = "صَاد",
            phonicsSound = "صَ.. صَ.. صَ",
            shapeStory = "حرف قوي وفخم يشبه عين الصقر الشجاع!",
            dailyWords = "صقر، صندوق، صابون، صاروخ، صحن",
            interactiveGame = "نغسل به أيدينا بالماء لتبقى نظيفة وبرائحة حلوة.. صـ.. صـ.. ما هو؟ (صابون)",
            soundAction = "صوت الصقر القوي يطير في السماء: صَ.. صَ.. صقر قوي!"
        ),
        "ض" to LetterCurriculum(
            letterChar = "ض",
            letterName = "ضَاد",
            phonicsSound = "ضَ.. ضَ.. ضَ",
            shapeStory = "أنا حرف الضاد المميز، لغتنا العربية الجميلة تسمى لغة الضاد!",
            dailyWords = "ضفدع، ضوء، ضرس، ضيف، ضمادة",
            interactiveGame = "كائن أخضر يقفز بالماء ويقول كواك كواك.. ضـ.. ضـ.. من هو؟ (ضفدع)",
            soundAction = "اقفز قفزة صغيرة كأنك ضفدع وقل: ضَ.. ضَ.. ضفدع يقفز!"
        ),
        "ط" to LetterCurriculum(
            letterChar = "ط",
            letterName = "طَاء",
            phonicsSound = "طَ.. طَ.. طَ",
            shapeStory = "شكلي يشبه طائرة تحلق في الفضاء بعصا عالية كالهوائي!",
            dailyWords = "طيارة، طبل، طماطم، طفل، طاولة",
            interactiveGame = "مركبة تطير في السماء فوق السحاب.. طـ.. طـ.. ما هي؟ (طيارة)",
            soundAction = "افرد ذراعيك كالطائرة وقل: طوووو.. طَ.. طَ.. طيارة تطير!"
        ),
        "ظ" to LetterCurriculum(
            letterChar = "ظ",
            letterName = "ظَاء",
            phonicsSound = "ظَ.. ظَ.. ظَ",
            shapeStory = "أنا حرف الظاء الفخم، مثل الطاء لكني أحمل نجمة متلألئة فوقي!",
            dailyWords = "ظرف، ظبي، ظل، ظفر، ظهر",
            interactiveGame = "نضع داخله الرسالة الجميلة ونرسلها لمن نحب.. ظـ.. ظـ.. ما هو؟ (ظرف)",
            soundAction = "أخرج لسانك بفخامة وقل: ظَ.. ظَ.. ظرف الرسائل!"
        ),
        "ع" to LetterCurriculum(
            letterChar = "ع",
            letterName = "عَيْن",
            phonicsSound = "عَ.. عَ.. عَ",
            shapeStory = "شكلي مثل عين جميلة نرى بها كل الأشياء الرائعة من حولنا!",
            dailyWords = "عصفور، عنب، عسل، علم، عين",
            interactiveGame = "شيء حلو ولذيذ جداً تصنعه النحلة النشيطة.. عَـ.. عَـ.. ما هو؟ (عسل)",
            soundAction = "رفرف بأصابعك كالعصفور المغرد وقل: صوصو.. عَ.. عَ.. عصفور!"
        ),
        "غ" to LetterCurriculum(
            letterChar = "غ",
            letterName = "غَيْن",
            phonicsSound = "غَ.. غَ.. غَ",
            shapeStory = "مثل العين لكن فوقي نقطة كأنها غيمة ماطرة في السماء الزرقاء!",
            dailyWords = "غزال، غيمة، غراب، غصن، غرفة",
            interactiveGame = "حيوان سريع وجميل جداً له قرون رشيقة.. غـ.. غـ.. من هو؟ (غزال)",
            soundAction = "انظر للسماء وتخيل الغيوم وقل: غَ.. غَ.. غيمة تمطر خيراً!"
        ),
        "ف" to LetterCurriculum(
            letterChar = "ف",
            letterName = "فَاء",
            phonicsSound = "فَ.. فَ.. فَ (فففف)",
            shapeStory = "صحن رشيق له رقبة جميلة وفوقه نقطة تزينه كالفراشة!",
            dailyWords = "فراشة، فيل، فواكه، فستان، فانوس",
            interactiveGame = "حيوان ضخم له خرطوم طويل وأذنان كبيرتان.. فـ.. فـ.. من هو؟ (فيل)",
            soundAction = "اطرد الهواء بلطف: فففف.. فَ.. فَ.. فراشة ملونة تطير!"
        ),
        "ق" to LetterCurriculum(
            letterChar = "ق",
            letterName = "قَاف",
            phonicsSound = "قَ.. قَ.. قَ",
            shapeStory = "صحن عميق يحمل نقطتين متألقتين مثل القطة التي تنظر إليك!",
            dailyWords = "قطة، قمر، قلم، قبعة، قلب",
            interactiveGame = "ينير السماء في الليل وشكله مثل الهلال أو الدائرة.. قـ.. قـ.. ما هو؟ (قمر)",
            soundAction = "قلد مواء القطة اللطيفة: مياو.. قَ.. قَ.. قطة صغيرة!"
        ),
        "ك" to LetterCurriculum(
            letterChar = "ك",
            letterName = "كَاف",
            phonicsSound = "كَ.. كَ.. كَ",
            shapeStory = "كرسي ملكي يحمل في وسطه همزة صغيرة تشبه الكاف اللطيفة!",
            dailyWords = "كلب، كتاب، كرة، كعكة، كرسي",
            interactiveGame = "نلعب بها في الملعب ونسدد أهدافاً رائعة.. كُـ.. كُـ.. ما هي؟ (كرة)",
            soundAction = "صوت الكلب اللطيف الوفي: هاو هاو.. كَ.. كَ.. كلب شجاع!"
        ),
        "ل" to LetterCurriculum(
            letterChar = "ل",
            letterName = "لَام",
            phonicsSound = "لَ.. لَ.. لَ",
            shapeStory = "شكلي يشبه عصا المظلة المقلوبة أو سنارة الصيد العجيبة!",
            dailyWords = "ليمون، لعبة، لبن، لسان، لؤلؤ",
            interactiveGame = "فاكهة صفراء حامضة ولذيذة نصنع منها عصيراً منعشاً.. لَـ.. لَـ.. ما هي؟ (ليمون)",
            soundAction = "غمض عينيك كأنك تذوقت ليموناً حامضاً وقل: لَ.. لَ.. ليمون حامض!"
        ),
        "م" to LetterCurriculum(
            letterChar = "م",
            letterName = "مِيم",
            phonicsSound = "مَ.. مَ.. مَ (مممم)",
            shapeStory = "رأس دائري لطيف يمتد منه خط مستقيم كالمشط الجميل!",
            dailyWords = "موز، ماء، مفتاح، مشط، مسجد",
            interactiveGame = "أنا جائع وفاكهتي المفضلة لونها أصفر يحبها القرد وتبدأ بحرفي.. ما هي؟ (موز)",
            soundAction = "تذوق أكلاً شهياً وقل: ممممم.. مَ.. مَ.. موز لذيذ!"
        ),
        "ن" to LetterCurriculum(
            letterChar = "ن",
            letterName = "نُون",
            phonicsSound = "نَ.. نَ.. نَ",
            shapeStory = "صحن هلالي عميق يضع في قلبه قطرة عسل كالنحلة!",
            dailyWords = "نحلة، نجمة، نملة، نمر، نظارة",
            interactiveGame = "تلمع في السماء ليلاً كالألماسة.. نـ.. نـ.. ما هي؟ (نجمة)",
            soundAction = "رفرف بأصابعك كالنحلة النشيطة وقل: زززز.. نَ.. نَ.. نحلة عسل!"
        ),
        "هـ" to LetterCurriculum(
            letterChar = "هـ",
            letterName = "هَاء",
            phonicsSound = "هَ.. هَ.. هَ",
            shapeStory = "دائرة كبيرة داخلها دائرة صغيرة تضحك دائماً بمرح!",
            dailyWords = "هرم، هدية، هلال، هاتف، هواء",
            interactiveGame = "صندوق مغلف بأشرطة ملونة نفتحه في عيد الميلاد.. هـ.. هـ.. ما هو؟ (هدية)",
            soundAction = "اضحك بصوت عالٍ: ها ها ها.. هَ.. هَ.. هدية جميلة!"
        ),
        "و" to LetterCurriculum(
            letterChar = "و",
            letterName = "وَاو",
            phonicsSound = "وَ.. وَ.. وَ",
            shapeStory = "رأس دائري لطيف يتزحلق بذيل مقوس كالوردة العطرة!",
            dailyWords = "وردة، ولد، وسادة، وجه، وطواط",
            interactiveGame = "نبات جميل وله رائحة زكية بألوان عطرة.. وَ.. وَ.. ما هو؟ (وردة)",
            soundAction = "شم رائحة وردة خيالية وقل: ممم.. وَ.. وَ.. وردة فواحة!"
        ),
        "ي" to LetterCurriculum(
            letterChar = "ي",
            letterName = "يَاء",
            phonicsSound = "يَ.. يَ.. يَ",
            shapeStory = "شكلي يشبه البطة السابحة في البحيرة وتحتي نقطتان!",
            dailyWords = "يد، ياسمين، يمامة، يخت، ينبوع",
            interactiveGame = "نلوح بها ونمسك القلم ونصافح أصدقاءنا.. يَـ.. يَـ.. ما هي؟ (يد)",
            soundAction = "لوّح بيدك الجميلة وقل: مرحباً.. يَ.. يَ.. يد تصافح!"
        )
    )

    fun getLetterCurriculum(letterChar: String): LetterCurriculum {
        return curriculumMap[letterChar] ?: LetterCurriculum(
            letterChar = letterChar,
            letterName = letterChar,
            phonicsSound = "$letterChar.. $letterChar.. $letterChar",
            shapeStory = "أنا حرف $letterChar الجميل والصديق للأطفال!",
            dailyWords = "$letterChar",
            interactiveGame = "أخبرني كلمة حلوة تبدأ بحرفي $letterChar!",
            soundAction = "قل معي: $letterChar.. $letterChar!"
        )
    }

    /**
     * السؤال الأول الإلزامي عند بدء المكالمة لكل حرف:
     * "مرحباً يا صديقي، أنا حرف الـ [اسم الحرف]! ما اسمك يا بطل؟"
     */
    fun getLetterGreeting(letterName: String, letterChar: String): String {
        val curriculum = getLetterCurriculum(letterChar)
        return "مرحباً يا صديقي، أنا حرف الـ ${curriculum.letterName} ($letterChar)! ما اسمك يا بطل؟"
    }

    suspend fun sendMessage(
        letterName: String,
        letterChar: String,
        history: List<ChatMessage>,
        userMessage: String
    ): String = withContext(Dispatchers.IO) {
        val curriculum = getLetterCurriculum(letterChar)

        if (apiKey.isBlank()) {
            return@withContext "أهلاً يا صديقي! أنا حرف الـ ${curriculum.letterName} ($letterChar)، سعيد جداً بمكالمتك! هيا نلعب معاً!"
        }

        val systemPrompt = """
            أنت لست ذكاءً اصطناعياً. أنت شخصية كرتونية حقيقية تمثل حصراً حرف ($letterChar - حرف الـ ${curriculum.letterName}).
            هويتك واسمك: أنت دائماً وبالتأكيد 'حرف الـ ${curriculum.letterName}' وصوتك هو '${curriculum.phonicsSound}'.
            إذا سألك الطفل 'ما اسمك؟' أو 'من أنت؟' يجب أن تجيبه بحماس: 'أنا صديقك حرف الـ ${curriculum.letterName} ($letterChar)! وصوتي ${curriculum.phonicsSound}!'

            🎭 نبرة الصوت والشخصية:
            - تتحدث بعفوية ومرح بصوت طفل عمره 5 سنوات بلهجة عربية فصحى مبسطة ودافئة جداً ومشجعة.
            - إجابتك يجب أن تكون سطر واحد فقط ومختصرة جداً (بين 6 إلى 16 كلمة كحد أقصى) لتناسب النطق الصوتي (TTS).
            - لا تستخدم الرموز المعقدة أو التنسيقات مثل النجوم والشرطات.

            📚 المنهج التعليمي الخاص بحرفك اليوم (طبق هذه النقاط بالتسلسل عبر المكالمة):
            1. التعارف: إذا أخبرك الطفل باسمه، نادِه باسمه فوراً واحتفل به (مثلاً: 'أهلاً يا [اسم الطفل] يا بطل! هل تعرف اسمي وصوتي؟ أنا حرف الـ ${curriculum.letterName}!').
            2. الصوتيات والحركات: علمه صوت الحرف (${curriculum.phonicsSound}) واسأله: 'تقدر تكرر معي: ${curriculum.phonicsSound}؟'.
            3. الخيال والشكل: صف له شكلك بطريقة ممتعة: '${curriculum.shapeStory}'.
            4. التحدي واللعبة التفاعلية: اطرح عليه اللغز أو التحدي الحركي: '${curriculum.interactiveGame}' أو '${curriculum.soundAction}'.
            5. التشجيع والمكافأة: إذا جاوب أو ردد، امدحه بحماس وفخر ('أنت بطل ذكي ورائع!').

            كلمات من بيئة الطفل تبدأ بحرفك: ${curriculum.dailyWords}.
            تفاعل مع كل ما يقوله الطفل بلطف واجعله يشعر بأنك صديقه الحقيقي الذي يلعب معه في مكالمة فيديو حية.
        """.trimIndent()

        try {
            val jsonBody = JSONObject().apply {
                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })

                // Contents with conversation history
                val contentsArray = JSONArray()
                // Previous turns (keep last 8 turns for memory and child's name persistence)
                for (msg in history.takeLast(8)) {
                    contentsArray.put(JSONObject().apply {
                        put("role", if (msg.role == "user") "user" else "model")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", msg.text) })
                        })
                    })
                }
                // Current user message
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userMessage) })
                    })
                })
                put("contents", contentsArray)

                // Generation Config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 80)
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext "أهلاً بك يا بطل! أنا حرف الـ ${curriculum.letterName} ($letterChar)، هيا نلعب ونردد صوت ${curriculum.phonicsSound}!"
            }

            val resJson = JSONObject(responseString)
            val candidates = resJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val rawText = parts.getJSONObject(0).optString("text", "").trim()
                    val cleaned = rawText
                        .replace("*", "")
                        .replace("#", "")
                        .replace("\n", " ")
                        .trim()
                    if (cleaned.isNotBlank()) {
                        return@withContext cleaned
                    }
                }
            }
            "أهلاً يا صديقي! أنا حرف الـ ${curriculum.letterName}، ما رأيك أن نلعب لعبة بحرفي؟"
        } catch (e: Exception) {
            "أهلاً يا بطل! أنا صديقك حرف الـ ${curriculum.letterName}، هيا قل معي: ${curriculum.phonicsSound}!"
        }
    }
}
