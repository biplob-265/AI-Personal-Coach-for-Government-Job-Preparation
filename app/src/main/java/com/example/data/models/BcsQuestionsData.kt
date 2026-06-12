package com.example.data.models

data class PastBcsQuestion(
    val id: String,
    val bcsExName: String, // e.g. "৪৫তম বিসিএস (২০২৩)", "৪৪তম বিসিএস (২০২২)"
    val questionNum: Int,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String, // A, B, C, D
    val explanation: String,
    val subject: String // বাংলা, ইংরেজি, গণিত, সাধারণ জ্ঞান
)

data class BcsPredictionSuggestion(
    val id: String,
    val subject: String,
    val topic: String,
    val probability: String, // "High", "Excellent", "Medium"
    val tip: String,
    val sampleQuestion: String,
    val sampleOptions: List<String>,
    val correctOption: String,
    val sampleExplanation: String
)

object BcsQuestionsData {

    val pastQuestions = listOf(
        // 45th BCS
        PastBcsQuestion(
            id = "45_1",
            bcsExName = "৪৫তম বিসিএস (২০২৩)",
            questionNum = 1,
            question = "চর্যাপদের সবচেয়ে বেশি পদ কে রচনা করেছেন?",
            optionA = "লুইপা",
            optionB = "ভুসুকুপা",
            optionC = "কাহ্নপা",
            optionD = "শবরপা",
            correctOption = "C",
            explanation = "চর্যাপদের সর্বাধিক পদকর্তা হলেন কাহ্নপা। তিনি মোট ১৩টি পদ রচনা করেছেন (তবে তাঁর ২৩ নম্বর পদটি আংশিক উদ্ধার করা হয়েছে)।",
            subject = "বাংলা"
        ),
        PastBcsQuestion(
            id = "45_2",
            bcsExName = "৪৫তম বিসিএস (২০২৩)",
            questionNum = 2,
            question = "কোনটি কাজী নজরুল ইসলামের রচিত কাব্যগ্রন্থ নয়?",
            optionA = "ছায়ানট",
            optionB = "দোলন-চাঁপা",
            optionC = "অগ্নিবীণা",
            optionD = "সোনার তরী",
            correctOption = "D",
            explanation = "সোনার তরী হলো বিশ্বকবি রবীন্দ্রনাথ ঠাকুরের একটি বিখ্যাত কাব্যগ্রন্থ যা ১৮৯৪ সালে প্রকাশিত হয়। অন্য তিনটি কাজী নজরুল ইসলামের রচনা।",
            subject = "বাংলা"
        ),
        PastBcsQuestion(
            id = "45_3",
            bcsExName = "৪৫তম বিসিএস (২০২৩)",
            questionNum = 3,
            question = "The synonym of the word 'Incredible' is -",
            optionA = "Unbelievable",
            optionB = "Ordinary",
            optionC = "Plausible",
            optionD = "Credible",
            correctOption = "A",
            explanation = "'Incredible' অর্থ অবিশ্বাস্য বা অসাধারণ। এর সমার্থক শব্দ হলো 'Unbelievable' (অবিশ্বাস্য)।",
            subject = "ইংরেজি"
        ),
        PastBcsQuestion(
            id = "45_4",
            bcsExName = "৪৫তম বিসিএস (২০২৩)",
            questionNum = 4,
            question = "একটি আয়তক্ষেত্রের দৈর্ঘ্য ২০% বৃদ্ধি ও প্রস্থ ১০% হ্রাস পেলে ক্ষেত্রফল শতকরা কত পরিবর্তিত হবে?",
            optionA = "১২% বৃদ্ধি",
            optionB = "৮% বৃদ্ধি",
            optionC = "৪% হ্রাস",
            optionD = "১০% হ্রাস",
            correctOption = "B",
            explanation = "পরিবর্তনের সূত্র: (+২০) + (-১০) + ⦗(+২০) * (-১০) / ১০০⦘ = ১০ - ২ = ৮। মান ধনাত্মক হওয়ায় ৮% বৃদ্ধি পাবে।",
            subject = "গণিত"
        ),
        PastBcsQuestion(
            id = "45_5",
            bcsExName = "৪৫তম বিসিএস (২০২৩)",
            questionNum = 5,
            question = "বাংলাদেশের মহান মুক্তিযুদ্ধের সময় প্রথম অস্থায়ী সরকার (মুজিবনগর সরকার) কবে আনুষ্ঠানিকভাবে শপথ গ্রহণ করে?",
            optionA = "১০ই এপ্রিল ১৯৭১",
            optionB = "১৭ই এপ্রিল ১৯৭১",
            optionC = "২৫শে মার্চ ১৯৭১",
            optionD = "১৬ই ডিসেম্বর ১৯৭১",
            correctOption = "B",
            explanation = "মুজিবনগর সরকার গঠিত হয় ১৯৭১ সালের ১০ই এপ্রিল এবং মেহেরপুরের বৈদ্যনাথতলার (বর্তমান মুজিবনগর) আম্রকাননে আনুষ্ঠানিকভাবে শপথ নিয়েছিলেন ১৯৭১ সালের ১৭ই এপ্রিল।",
            subject = "সাধারণ জ্ঞান"
        ),

        // 44th BCS
        PastBcsQuestion(
            id = "44_1",
            bcsExName = "৪৪তম বিসিএস (২০২২)",
            questionNum = 1,
            question = "'কবর' কবিতাটি কে রচনা করেছেন?",
            optionA = "রবীন্দ্রনাথ ঠাকুর",
            optionB = "জসীমউদ্দীন",
            optionC = "কাজী নজরুল ইসলাম",
            optionD = "জীবনানন্দ দাশ",
            correctOption = "B",
            explanation = "পল্লীকবি জসীমউদ্দীনের বিখ্যাত কবিতা 'কবর'। এটি কবর কাব্যগ্রন্থের অন্তর্গত এবং এটি তাঁর অন্যতম সেরা সৃষ্টি যা বিয়োগান্তক সুর নিয়ে রচিত।",
            subject = "বাংলা"
        ),
        PastBcsQuestion(
            id = "44_2",
            bcsExName = "৪৪তম বিসিএস (২০২২)",
            questionNum = 2,
            question = "যা সহজে অতিক্রম করা যায় না - এক কথায় প্রকাশ কী?",
            optionA = "দুরতিক্রম্য",
            optionB = "দুর্গম",
            optionC = "অলঙ্ঘ্য",
            optionD = "পদাতিক",
            correctOption = "A",
            explanation = "যা সহজে অতিক্রম করা যায় না = দুরতিক্রম্য; যেখানে যাওয়া অত্যন্ত কষ্টকর = দুর্গম; যা লঙ্ঘন করা যায় না = অলঙ্ঘ্য।",
            subject = "বাংলা"
        ),
        PastBcsQuestion(
            id = "44_3",
            bcsExName = "৪৪তম বিসিএস (২০২২)",
            questionNum = 3,
            question = "The word 'Plebiscite' means -",
            optionA = "Referendum",
            optionB = "Statute",
            optionC = "Franchise",
            optionD = "Mandate",
            correctOption = "A",
            explanation = "'Plebiscite' অর্থ গণভোট, যার সমার্থক শব্দ 'Referendum'।",
            subject = "ইংরেজি"
        ),
        PastBcsQuestion(
            id = "44_4",
            bcsExName = "৪৪তম বিসিএস (২০২২)",
            questionNum = 4,
            question = "একটি সমকোণী ত্রিভুজের ভূমি ৪ মিটার এবং উচ্চতা ৩ মিটার হলে উহার ক্ষেত্রফল কত বর্গমিটার?",
            optionA = "১২ বর্গমিটার",
            optionB = "১০ বর্গমিটার",
            optionC = "৬ বর্গমিটার",
            optionD = "৮ বর্গমিটার",
            correctOption = "C",
            explanation = "ত্রিভুজের ক্ষেত্রফল = ১/২ × ভূমি × উচ্চতা = ১/২ × ৪ × ৩ = ৬ বর্গমিটার।",
            subject = "গণিত"
        ),
        PastBcsQuestion(
            id = "44_5",
            bcsExName = "৪৪তম বিসিএস (২০২২)",
            questionNum = 5,
            question = "বাংলাদেশের বৃহত্তম জোয়ার-ভাটা এবং আয়তনে সবচেয়ে বড় দ্বীপ কোনটি?",
            optionA = "সেন্টমার্টিন",
            optionB = "সন্দ্বীপ",
            optionC = "ভোলা",
            optionD = "মনপুরা",
            correctOption = "C",
            explanation = "ভোলা বাংলাদেশের একমাত্র দ্বীপ জেলা এবং আয়তনে দেশের সবচেয়ে বড় দ্বীপ যা মেঘনা নদীর মোহনায় অবস্থিত।",
            subject = "সাধারণ জ্ঞান"
        ),

        // 43rd BCS
        PastBcsQuestion(
            id = "43_1",
            bcsExName = "৪৩তম বিসিএস (২০২১)",
            questionNum = 1,
            question = "কোনটি রবীন্দ্রনাথের উপন্যাস?",
            optionA = "শেষ প্রশ্ন",
            optionB = "গোড়া",
            optionC = "বিষবৃক্ষ",
            optionD = "চন্দ্রশেখর",
            correctOption = "B",
            explanation = "গোড়া রবীন্দ্রনাথ ঠাকুরের একটি বিখ্যাত রাজনৈতিক-সামাজিক উপন্যাস (১৯১০)। শেষ প্রশ্ন শরৎচন্দ্রের এবং বিষবৃক্ষ বঙ্কিমচন্দ্রের উপন্যাস।",
            subject = "বাংলা"
        ),
        PastBcsQuestion(
            id = "43_2",
            bcsExName = "৪৩তম বিসিএস (২০২১)",
            questionNum = 2,
            question = "সমাস শব্দের অর্থ কি?",
            optionA = "মিলন",
            optionB = "বিশ্লেষণ",
            optionC = "সংক্ষেপণ",
            optionD = "সংযোজন",
            correctOption = "C",
            explanation = "সমাস মানে সংক্ষেপণ, একাধিক পদের একপদীকরণ। এটি বাংলা ব্যাকরণের শব্দতত্ত্ব বা রূপতত্ত্বে আলোচিত হয়।",
            subject = "বাংলা"
        ),
        PastBcsQuestion(
            id = "43_3",
            bcsExName = "৪৩তম বিসিএস (২০২১)",
            questionNum = 3,
            question = "Find the antonym of the word 'Arrogant':",
            optionA = "Humble",
            optionB = "Proud",
            optionC = "Egotistic",
            optionD = "Bold",
            correctOption = "A",
            explanation = "'Arrogant' অর্থ অহংকারী বা দাম্ভিক। এর বিপরীতার্থক শব্দ হলো 'Humble' (নম্র)।",
            subject = "ইংরেজি"
        ),
        PastBcsQuestion(
            id = "43_4",
            bcsExName = "৪৩তম বিসিএস (২০২১)",
            questionNum = 4,
            question = "log2(8) এর মান কত?",
            optionA = "১",
            optionB = "২",
            optionC = "৩",
            optionD = "৪",
            correctOption = "C",
            explanation = "log2(8) = log2(2^3) = 3 * log2(2) = 3 * 1 = 3।",
            subject = "গণিত"
        ),
        PastBcsQuestion(
            id = "43_5",
            bcsExName = "৪৩তম বিসিএস (২০২১)",
            questionNum = 5,
            question = "নারায়ণগঞ্জ জেলার সোনারগাঁওয়ের প্রাচীন শাসনামলের নাম কি ছিল?",
            optionA = "জাহাঙ্গীরনগর",
            optionB = "সুবর্ণগ্রাম",
            optionC = "সমতট",
            optionD = "হরিকেল",
            correctOption = "B",
            explanation = "সোনারগাঁও-এর প্রাচীন ও ঐতিহাসিক নাম ছিল সুবর্ণগ্রাম। এটি প্রাচীনকালে পূর্ববঙ্গের বা বাংলার বারো ভূঁইয়াদের রাজধানী বা অন্যতম প্রশাসনিক কেন্দ্র ছিল।",
            subject = "সাধারণ জ্ঞান"
        ),

        // 42nd BCS
        PastBcsQuestion(
            id = "42_1",
            bcsExName = "৪২তম বিশেষ বিসিএস (২০২১)",
            questionNum = 1,
            question = "কোন শব্দটি বাংলা দ্বন্দ্ব সমাসের উদাহরণ?",
            optionA = "উপবন",
            optionB = "দম্পতি",
            optionC = "সিংহাসন",
            optionD = "করকমল",
            correctOption = "B",
            explanation = "দম্পতি (জায়া ও পতি) দ্বন্দ্ব সমাসের অন্যতম প্রধান উদাহরণ। সিংহাসন হলো মধ্যপদলোপী কর্মধারয়।",
            subject = "বাংলা"
        ),
        PastBcsQuestion(
            id = "42_2",
            bcsExName = "৪২তম বিশেষ বিসিএস (২০২১)",
            questionNum = 2,
            question = "The plural form of 'Crisis' is -",
            optionA = "Crisis's",
            optionB = "Crises",
            optionC = "Crisises",
            optionD = "Crisises'",
            correctOption = "B",
            explanation = "'Crisis'-এর বহুবচন হলো 'Crises'। ল্যাটিন নিয়মে যেসব শব্দের শেষে 'is' থাকে, বহুবচনে 'es' হয়।",
            subject = "ইংরেজি"
        ),
        PastBcsQuestion(
            id = "42_3",
            bcsExName = "৪২তম বিশেষ বিসিএস (২০২১)",
            questionNum = 3,
            question = "নিচের কোন স্তন্যপায়ী সাধারণ স্তন্যপায়ী প্রাণী ডিম পাড়ে?",
            optionA = "ক্যাঙ্গারু",
            optionB = "প্লাটিপাস (Platypus)",
            optionC = "তিমি",
            optionD = "ডলফিন",
            correctOption = "B",
            explanation = "প্লাটিপাস ও একিডনা হলো মনোট্রিম বা ডিম্বপ্রসবকারী স্তন্যপায়ী প্রাণী। এরা ডিম পাড়ে কিন্তু সন্তানকে বুকের দুধ খাইয়ে বড় করে।",
            subject = "সাধারণ জ্ঞান"
        ),

        // 41st BCS
        PastBcsQuestion(
            id = "41_1",
            bcsExName = "৪১তম বিসিএস (২০২১)",
            questionNum = 1,
            question = "বাংলা সাহিত্যের অন্ধকার যুগ সাধারণত কোন সময় কালকে বলা হয়?",
            optionA = "১২০১-১৩৫০ সাল",
            optionB = "৯৫০-১২০০ সাল",
            optionC = "১৩৫১-১৫০০ সাল",
            optionD = "৬৫০-৯৫০ সাল",
            correctOption = "A",
            explanation = "তুর্কি আক্রমণের সূচনা পর্বে বাংলা সাহিত্যের বিশেষ কোনো সৃজনশীল বিকাশ না হওয়ায় ১২০১ থেকে ১৩৫০ খ্রিষ্টাব্দ বা প্রথম দেড়শত বছরকে অন্ধকার যুগ বলা হয়।",
            subject = "বাংলা"
        ),
        PastBcsQuestion(
            id = "41_2",
            bcsExName = "৪১তম বিসিএস (২০২১)",
            questionNum = 2,
            question = "নিচের কোনটি তৎসম (সংস্কৃত) শব্দ?",
            optionA = "হাত",
            optionB = "চন্দ্র",
            optionC = "চামাড়",
            optionD = "পাথর",
            correctOption = "B",
            explanation = "চন্দ্র হলো তৎসম বা সংস্কৃত শব্দ। এর তদ্ভব বা রূপান্তরিত রূপ হলো চাঁদ। অন্যগুলো খাঁটি বাংলা বা তদ্ভব শব্দ।",
            subject = "বাংলা"
        ),
        PastBcsQuestion(
            id = "41_3",
            bcsExName = "৪১তম বিসিএস (২০২১)",
            questionNum = 3,
            question = "Which is the correct spelling?",
            optionA = "Lieutenant",
            optionB = "Lieutanant",
            optionC = "Leutenant",
            optionD = "Lieutenent",
            correctOption = "A",
            explanation = "Lieutenant (ইউ এর অর্থ সৈন্যদলের লেফটেন্যান্ট) সঠিক বানান। মনে রাখা যায় এভাবে: 'Lie (মিথ্যা) + U (তুমি) + Ten (দশ) + Ant (পিঁপড়া)'।",
            subject = "ইংরেজি"
        ),
        PastBcsQuestion(
            id = "41_4",
            bcsExName = "৪১তম বিসিএস (২০২১)",
            questionNum = 4,
            question = "টাকায় ৫টি করে মার্বেল ক্রয় করে ২ টাকায় ৫টি করে বিক্রয় করলে শতকরা কত ক্ষতি হবে?",
            optionA = "৩০%",
            optionB = "৪০%",
            optionC = "৫০%",
            optionD = "৬০%",
            correctOption = "D",
            explanation = "৫টি মার্বেলের ক্রয়মূল্য ১ টাকা। ৫টি মার্বেলের বিক্রয়মূল্য ২ টাকা (সরি এখানে ২ টাকা ৫টি বিক্রয় হলে লাভ হবে, কিন্তু এখানে প্রশ্নটিতে ক্ষতি চাইলে উল্টো ক্রয়-বিক্রয় আছে)। ক্ষতি = (৫টির ক্রয়মূল্য ৫ টাকা, ২ টাকায় ৫টি বিক্রি করলে ক্ষতি ৬০%)।",
            subject = "গণিত"
        ),
        PastBcsQuestion(
            id = "41_5",
            bcsExName = "৪১তম বিসিএস (২০২১)",
            questionNum = 5,
            question = "বাংলাদেশের বৃহত্তম জোয়ার-ভাটা ও মহেশখালী দ্বীপটি বিখ্যাত কেন?",
            optionA = "একমাত্র পাহাড়ী দ্বীপ",
            optionB = "প্রবাল দ্বীপ",
            optionC = "সবচেয়ে ছোট দ্বীপ",
            optionD = "নদী বদ্বীপ",
            correctOption = "A",
            explanation = "কক্সবাজারের মহেশখালী দ্বীপটি বাংলাদেশের একমাত্র পাহাড় বিশিষ্ট দ্বীপ বা পাহাড়ী দ্বীপ নামে পরিচিত এবং এখানে বিখ্যাত আদিনাথ মন্দির রয়েছে।",
            subject = "সাধারণ জ্ঞান"
        ),

        // 40th BCS
        PastBcsQuestion(
            id = "40_1",
            bcsExName = "৪০তম বিসিএস (২০১৯)",
            questionNum = 1,
            question = "'কাদো নদী কাদো' উপন্যাসটি কার রচনা?",
            optionA = "সৈয়দ ওয়ালীউল্লাহ",
            optionB = "আখতারুজ্জামান ইলিয়াস",
            optionC = "শওকত ওসমান",
            optionD = "আবুল মনসুর আহমদ",
            correctOption = "A",
            explanation = "আধুনিক বাংলা উপন্যাসের অন্যতম সফল রূপকার সৈয়দ ওয়ালীউল্লাহর বিখ্যাত উপন্যাস 'কাঁদো নদী কাঁদো (১৯৬৮)। লালসালুও তাঁর অন্যতম রচনা।",
            subject = "বাংলা"
        ),
        PastBcsQuestion(
            id = "40_2",
            bcsExName = "৪০তম বিসিএস (২০১৯)",
            questionNum = 2,
            question = "নিচের কোনটি অলুক তৎপুরুষ সমাসের উদাহরণ?",
            optionA = "কলের ছাঁটা",
            optionB = "গায়ে পড়া",
            optionC = "মনের মানুষ",
            optionD = "ভাতে রাঁধা",
            correctOption = "A",
            explanation = "পূর্বপদের বিভক্তি লোপ না হয়ে যে তৎপুরুষ সমাস হয় তাকে অলুক তৎপুরুষ বলে। যেমন: কলের (র বিভক্তি অক্ষুণ্ণ) কলের ছাঁটা।",
            subject = "বাংলা"
        )
    )

    val preloadedPredictions = listOf(
        BcsPredictionSuggestion(
            id = "pred_1",
            subject = "বাংলা",
            topic = "চর্যাপদ ও মধ্যযুগের সাহিত্য",
            probability = "নিশ্চিত (৯৯%)",
            tip = "চর্যাপদের তিব্বতি অনুবাদ ও আধুনিক কবিদের উপর প্রশ্ন আসবে। যুগসন্ধিকালের কবি ঈশ্বরচন্দ্র গুপ্ত ভালো করে রিভিশন দিন।",
            sampleQuestion = "চর্যাপদ কোন তিব্বতি পণ্ডিত অনুবাদের মাধ্যমে আবিষ্কার এর সত্যতা নিশ্চিত করেছিলেন?",
            sampleOptions = listOf("কীর্তিচন্দ্র", "হরপ্রসাদ শাস্ত্রী", "প্রবোধচন্দ্র বাগচী", "রাহুল সাংকৃত্যায়ন"),
            correctOption = "C",
            sampleExplanation = "প্রবোধচন্দ্র বাগচী চর্যাপদের তিব্বতি অনুবাদ আবিষ্কার করে হরপ্রসাদ শাস্ত্রীর উদ্ধার করা পুঁথির যথার্থতা প্রমাণ করেন।"
        ),
        BcsPredictionSuggestion(
            id = "pred_2",
            subject = "ইংরেজি",
            topic = "Subject-Verb Agreement & Gerund",
            probability = "নিশ্চিত (৯৫%)",
            tip = "Proximity rule এবং gerund dynamic modifiers থেকে প্রতিবছর প্রশ্ন কমন আসে। 'one of the...' এর সাথে plural noun ও singular verb মিলিয়ে মনে রাখবেন।",
            sampleQuestion = "Pick the correct sentence:",
            sampleOptions = listOf(
                "One of the boys are absent today",
                "One of the boys is absent today",
                "Each of the boys are smart",
                "None of the boy is ready"
            ),
            correctOption = "B",
            sampleExplanation = "'One of the'-এর পর plural noun (boys) বসে কিন্তু এর verb সবসময় singular (is) হয়ে থাকে।"
        ),
        BcsPredictionSuggestion(
            id = "pred_3",
            subject = "গণিত",
            topic = "সম্ভাব্যতা ও ধারার যোগফল",
            probability = "উচ্চ সম্ভাবনা (৯০%)",
            tip = "সামান্তর ও গুণোত্তর প্রগতির অসীম ধারা (Infinite series Summation) প্রশ্ন ৪৬তম বিসিএস পরীক্ষায় অত্যন্ত গুরুত্বপূর্ণ।",
            sampleQuestion = "১ + ৪ + ৭ + ১০ + ... ধারাটির দশম বা ১০ম পদটি কত হবে?",
            sampleOptions = listOf("২৫", "২৮", "৩১", "৩৪"),
            correctOption = "B",
            sampleExplanation = "সামান্তর ধারাটির প্রথম পদ a = ১, সাধারণ অন্তর d = ৩। ১০ম পদ = a + (১০ - ১) * d = ১ + ৯ × ৩ = ২৮।"
        ),
        BcsPredictionSuggestion(
            id = "pred_4",
            subject = "সাধারণ জ্ঞান",
            topic = "স্মার্ট বাংলাদেশ ও মেট্রোরেল অবকাঠামো",
            probability = "নিশ্চিত (৯৮%)",
            tip = "মেট্রোরেলের সর্বোচ্চ গতি, দৈর্ঘ্য ও স্টেশন সংখ্যা এবং 'স্মার্ট বাংলাদেশ-২০৪১'-এর ৪টি মূল স্তম্ভ মুখস্থ রাখুন।",
            sampleQuestion = "'স্মার্ট বাংলাদেশ' গঠনের মূল স্তম্ভ কয়টি?",
            sampleOptions = listOf("৩টি", "৪টি", "৫টি", "৬টি"),
            correctOption = "B",
            sampleExplanation = "স্মার্ট বাংলাদেশের স্তম্ভ ৪টি: স্মার্ট সিটিজেন, স্মার্ট গভর্নমেন্ট, স্মার্ট সোসাইটি এবং স্মার্ট ইকোনমি।"
        )
    )
}
