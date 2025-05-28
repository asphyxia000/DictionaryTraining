package com.example.vkr2.DataBase.Migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDateTime

val MIGRATION_1_2 = object : Migration(1,2){
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
                    CREATE TABLE IF NOT EXISTS Exercises (
                        ExercisesId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        ExercisesName TEXT NOT NULL,
                        muscleGroupID INTEGER NOT NULL,
                        FOREIGN KEY (muscleGroupID) REFERENCES MuscleGroup (MuscleGroupsID) ON DELETE CASCADE
                    )
                    """.trimIndent())
    }

}

val MIGRATION_2_3 = object : Migration(2,3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(

            "INSERT INTO Exercises (ExercisesName, muscleGroupID) VALUES " +
                    "('Жим лёжа (наклон)', 1), " +
                    "('Сведение рук', 1), " +
                    "('Жим от груди', 1), " +
                    "('Пуловер', 1), " +
                    "('Отжимания в наклоне', 1), " +
                    "('Брусья', 1), " +

                    "('Сгибание рук', 2), " +
                    "('Сгибания с супинацией', 2), " +
                    "('Подтягивания обратным хватом', 2), " +
                    "('Сгибание рук в Скотте', 2), " +
                    "('Жим лёжа узким хватом', 2), " +
                    "('Разгибание рук', 2), " +
                    "('Алмазные отжимания ', 2), " +
                    "('Французский жим', 2), " +

                    "('Гакк-приседания', 3), " +
                    "('Фронтальные приседания', 3), " +
                    "('Сплит-преседания', 3), " +
                    "('Болгарские приседания', 3), " +
                    "('Жим ногами', 3), " +
                    "('Становая тяга', 3), " +
                    "('Румынксая тяга', 3), " +
                    "('Выпады', 3), " +
                    "('Сгибания', 3), " +
                    "('Разгибания', 3), " +
                    "('Сведение', 3), " +
                    "('Разведение', 3), " +
                    "('Ягодичный мостик', 3), " +
                    "('Махи ногой', 3), " +

                    "('Тяга в наклоне', 4), " +
                    "('Горизонтальная тяга', 4), " +
                    "('Вертикальная тяга', 4), " +
                    "('Тяга лёжа', 4), " +
                    "('Подтягивания', 4), " +
                    "('Шраги', 4), " +
                    "('Гиперэкстензия', 4), " +
                    "('Становая тяга', 4), " +

                    "('Жим над головой', 5), " +
                    "('Жим из за головы', 5), " +
                    "('Разведение рук', 5), " +
                    "('Подъём рук в стороны', 5), " +
                    "('Тяга к подбородку', 5), " +
                    "('Тяга на задние дельты', 5), " +
                    "('Отжимания уголком', 5), " +

                    "('Скручивания', 6), " +
                    "('Обратные скручивания', 6), " +
                    "('Подтягивания коленей к груди', 6), " +
                    "('Ситапы', 6), " +
                    "('Велосипед для пресса', 6), " +
                    "('Подъём ног в висе', 6), " +
                    "('Подъём коленей', 6), " +
                    "('Боковая планка', 6), " +
                    "('Махи ногами', 6), " +
                    "('Касание пяток лежа на спине', 6), " +

                    "('Беговая дорожка', 7), " +
                    "('Эллипсоид', 7), " +
                    "('Велосипед', 7), " +
                    "('Степпер', 7), " +
                    "('Скакалка', 7), " +
                    "('Гребля', 7), " +
                    "('Скалолаз', 7), " +
                    "('Бег с высоким поднимание ног', 7), " +
                    "('Захлест голени', 7);"
        )
    }
}

val MIGRATION_3_4 = object : Migration(3,4) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("DELETE FROM MuscleGroup WHERE MuscleGroupsID = 8")
        db.execSQL("DELETE FROM Exercises WHERE ExercisesId = 8")
    }
}

val MIGRATION_4_5 = object : Migration(4,5) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // 1. Создаём таблицу Tags
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS Tags (
                TagsId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL
            )
        """.trimIndent()
        )

        // 2. Вставляем теги
        db.execSQL(
            """
            INSERT INTO Tags (name) VALUES 
                ('Штанга'),
                ('Свободный вес'),
                ('Свой вес'),
                ('Гантели'),
                ('Тренажер'),
                ('Середина'),
                ('Верх'),
                ('Низ'),
                ('Бицепс'),
                ('Трицепс'),
                ('Предплечье'),
                ('Широчайшие'),
                ('Середина'),
                ('Поясница'),
                ('Трапеция'),
                ('Квадрицепс'),
                ('Бицепс Бедра'),
                ('Ягодицы'),
                ('Икры'),
                ('Внутр'),
                ('Середина'),
                ('Перед'),
                ('Задн'),
                ('Пресс'),
                ('Косые')
        """.trimIndent()
        )


        // 3. Создаём связующую таблицу TagsExercises (если вдруг её нет)
        db.execSQL(
            """
        CREATE TABLE IF NOT EXISTS TagsExercises (
            exerciseId INTEGER NOT NULL,
            tagsId INTEGER NOT NULL,
            PRIMARY KEY (exerciseId, tagsId),
            FOREIGN KEY (exerciseId) REFERENCES Exercises(ExercisesId) ON DELETE CASCADE,
            FOREIGN KEY (tagsId) REFERENCES Tags(TagsId) ON DELETE CASCADE)
        """.trimIndent()
        )

        // Добавь индексы:
        db.execSQL("CREATE INDEX IF NOT EXISTS index_TagsExercises_exerciseId ON TagsExercises(exerciseId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_TagsExercises_tagsId ON TagsExercises(tagsId)")

    }
}
val MIGRATION_5_6 = object : Migration(5,6){
    override fun migrate(db: SupportSQLiteDatabase){
            db.execSQL("""
                Create table if not exists Trainings(
                trainingId Integer primary key autoincrement not null,
                date text not null,
                name text not null,
                comment text not null
                )
            """.trimIndent())
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS TrainingExercises (
                trainingId INTEGER NOT NULL,
                exerciseId INTEGER NOT NULL,
                PRIMARY KEY(trainingId, exerciseId),
                FOREIGN KEY(trainingId) REFERENCES Trainings(trainingId) ON DELETE CASCADE,
                FOREIGN KEY(exerciseId) REFERENCES Exercises(ExercisesId) ON DELETE CASCADE
            )
            """.trimIndent())

            db.execSQL("CREATE INDEX IF NOT EXISTS index_TrainingExercises_exerciseId ON TrainingExercises(exerciseId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_TrainingExercises_trainingId ON TrainingExercises(trainingId)")

            db.execSQL("""
                 CREATE TABLE IF NOT EXISTS Sets (
                setId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                trainingId INTEGER NOT NULL,
                exerciseId INTEGER NOT NULL,
                reps INTEGER,
                weight REAL,
                duration INTEGER,
                exerciseOrder INTEGER NOT NULL,
                FOREIGN KEY(trainingId) REFERENCES Trainings(trainingId) ON DELETE CASCADE,
                FOREIGN KEY(exerciseId) REFERENCES Exercises(ExercisesId) ON DELETE CASCADE
                )
            """.trimIndent())

            db.execSQL("CREATE INDEX IF NOT EXISTS index_Sets_exerciseId ON Sets(exerciseId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_Sets_trainingId ON Sets(trainingId)")
    }

}
val MIGRATION_6_7 = object : Migration(6,7){
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE trainings ADD COLUMN createdAt TEXT NOT NULL DEFAULT ''")

        val now = LocalDateTime.now().toString()
        db.execSQL("UPDATE Trainings SET createdAt = '$now' WHERE createdAt = ''")
    }
}
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Создаём новую таблицу Sets_temp с weight INTEGER
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS Sets_temp (
                setId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                trainingId INTEGER NOT NULL,
                exerciseId INTEGER NOT NULL,
                reps INTEGER,
                weight INTEGER, -- заменили с REAL на INTEGER
                duration INTEGER,
                exerciseOrder INTEGER NOT NULL,
                FOREIGN KEY(trainingId) REFERENCES Trainings(trainingId) ON DELETE CASCADE,
                FOREIGN KEY(exerciseId) REFERENCES Exercises(ExercisesId) ON DELETE CASCADE
            )
        """.trimIndent())

        // 2. Копируем данные из старой таблицы в новую (приводим weight к целому)
        db.execSQL("""
            INSERT INTO Sets_temp (setId, trainingId, exerciseId, reps, weight, duration, exerciseOrder)
            SELECT setId, trainingId, exerciseId, reps, CAST(weight AS INTEGER), duration, exerciseOrder
            FROM Sets
        """.trimIndent())

        // 3. Удаляем старую таблицу
        db.execSQL("DROP TABLE Sets")

        // 4. Переименовываем новую таблицу в Sets
        db.execSQL("ALTER TABLE Sets_temp RENAME TO Sets")

        // 5. Восстанавливаем индексы
        db.execSQL("CREATE INDEX IF NOT EXISTS index_Sets_exerciseId ON Sets(exerciseId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_Sets_trainingId ON Sets(trainingId)")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Exercises ADD COLUMN imagePath TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val updates = listOf(
            "UPDATE Exercises SET imagePath = 'zhim_lezha_naklon.webp' WHERE ExercisesName = 'Жим лёжа (наклон)'",
            "UPDATE Exercises SET imagePath = 'svedenie_ruk.webp' WHERE ExercisesName = 'Сведение рук'",
            "UPDATE Exercises SET imagePath = 'zhim_ot_grudi.webp' WHERE ExercisesName = 'Жим от груди'",
            "UPDATE Exercises SET imagePath = 'pullover_gantely.webp' WHERE ExercisesName = 'Пуловер'",
            "UPDATE Exercises SET imagePath = 'otzhimanya_naklon.webp' WHERE ExercisesName = 'Отжимания в наклоне'",
            "UPDATE Exercises SET imagePath = 'otzhimanyia_na_brusiakh.webp' WHERE ExercisesName = 'Брусья'",

            "UPDATE Exercises SET imagePath = 'sgibanie_ruk.webp' WHERE ExercisesName = 'Сгибание рук'",
            "UPDATE Exercises SET imagePath = 'sgibanie_ruk_sup.webp' WHERE ExercisesName = 'Сгибания с супинацией'",
            "UPDATE Exercises SET imagePath = 'pogtygivanie_obrat.webp' WHERE ExercisesName = 'Подтягивания обратным хватом'",
            "UPDATE Exercises SET imagePath = 'scott_sitzend.webp' WHERE ExercisesName = 'Сгибание рук в Скотте'",
            "UPDATE Exercises SET imagePath = 'zhim_uzky_triceps.webp' WHERE ExercisesName = 'Жим лёжа узким хватом'",
            "UPDATE Exercises SET imagePath = 'overhead_razgib_ruk.webp' WHERE ExercisesName = 'Разгибание рук'",
            "UPDATE Exercises SET imagePath = 'almaz.webp' WHERE ExercisesName = 'Алмазные отжимания '",
            "UPDATE Exercises SET imagePath = 'franch_otzhim.webp' WHERE ExercisesName = 'Французский жим'",

            "UPDATE Exercises SET imagePath = 'gakk_prised.webp' WHERE ExercisesName = 'Гакк-приседания'",
            "UPDATE Exercises SET imagePath = 'front_squats.webp' WHERE ExercisesName = 'Фронтальные приседания'",
            "UPDATE Exercises SET imagePath = 'split_prised.webp' WHERE ExercisesName = 'Сплит-преседания'",
            "UPDATE Exercises SET imagePath = 'bulgarian_split_squats.webp' WHERE ExercisesName = 'Болгарские приседания'",
            "UPDATE Exercises SET imagePath = 'zhim_nogamy.webp' WHERE ExercisesName = 'Жим ногами'",
            "UPDATE Exercises SET imagePath = 'stanovay.webp' WHERE ExercisesName = 'Становая тяга'",
            "UPDATE Exercises SET imagePath = 'rum_tyaga.webp' WHERE ExercisesName = 'Румынксая тяга'",
            "UPDATE Exercises SET imagePath = 'split_squats.webp' WHERE ExercisesName = 'Выпады'",
            "UPDATE Exercises SET imagePath = 'sgib_nog.webp' WHERE ExercisesName = 'Сгибания'",
            "UPDATE Exercises SET imagePath = 'razgib_nog.webp' WHERE ExercisesName = 'Разгибания'",
            "UPDATE Exercises SET imagePath = 'svedenye_nog.webp' WHERE ExercisesName = 'Сведение'",
            "UPDATE Exercises SET imagePath = 'razvedenye_nog.webp' WHERE ExercisesName = 'Разведение'",
            "UPDATE Exercises SET imagePath = 'glute_bridge.webp' WHERE ExercisesName = 'Ягодичный мостик'",
            "UPDATE Exercises SET imagePath = 'mahy_nog_nazad.webp' WHERE ExercisesName = 'Махи ногой'",

            "UPDATE Exercises SET imagePath = 'tyaga_v_naklone.webp' WHERE ExercisesName = 'Тяга в наклоне'",
            "UPDATE Exercises SET imagePath = 'gorizont_tyaga.webp' WHERE ExercisesName = 'Горизонтальная тяга'",
            "UPDATE Exercises SET imagePath = 'vert_tyaga.webp' WHERE ExercisesName = 'Вертикальная тяга'",
            "UPDATE Exercises SET imagePath = 'greblya.webp' WHERE ExercisesName = 'Тяга лёжа'",
            "UPDATE Exercises SET imagePath = 'pogtygivanie.webp' WHERE ExercisesName = 'Подтягивания'",
            "UPDATE Exercises SET imagePath = 'dumbbell_shrug.webp' WHERE ExercisesName = 'Шраги'",
            "UPDATE Exercises SET imagePath = 'backextension.webp' WHERE ExercisesName = 'Гиперэкстензия'",

            "UPDATE Exercises SET imagePath = 'zhim_nad_golovoy.webp' WHERE ExercisesName = 'Жим над головой'",
            "UPDATE Exercises SET imagePath = 'zhim_nad_golovoy.webp' WHERE ExercisesName = 'Жим из за головы'",
            "UPDATE Exercises SET imagePath = 'zad_delta_tyaga.webp' WHERE ExercisesName = 'Разведение рук'",
            "UPDATE Exercises SET imagePath = 'podyem_ruk_vstorony.webp' WHERE ExercisesName = 'Подъём рук в стороны'",
            "UPDATE Exercises SET imagePath = 'tyaga_kpodborodku.webp' WHERE ExercisesName = 'Тяга к подбородку'",
            "UPDATE Exercises SET imagePath = 'zad_delta_tyaga.webp' WHERE ExercisesName = 'Тяга на задние дельты'",
            "UPDATE Exercises SET imagePath = 'ugolok.webp' WHERE ExercisesName = 'Отжимания уголком'",

            "UPDATE Exercises SET imagePath = 'sit_up.webp' WHERE ExercisesName = 'Скручивания'",
            "UPDATE Exercises SET imagePath = 'skruckivaniya_inverse.webp' WHERE ExercisesName = 'Обратные скручивания'",
            "UPDATE Exercises SET imagePath = 'koleni_k_grudi.webp' WHERE ExercisesName = 'Подтягивания коленей к груди'",
            "UPDATE Exercises SET imagePath = 'sit_up.webp' WHERE ExercisesName = 'Ситапы'",
            "UPDATE Exercises SET imagePath = 'velosiped.webp' WHERE ExercisesName = 'Велосипед для пресса'",
            "UPDATE Exercises SET imagePath = 'podye_nog_vis.webp' WHERE ExercisesName = 'Подъём ног в висе'",
            "UPDATE Exercises SET imagePath = 'podem_kolen_vys.webp' WHERE ExercisesName = 'Подъём коленей'",
            "UPDATE Exercises SET imagePath = 'bok_planka.webp' WHERE ExercisesName = 'Боковая планка'",
            "UPDATE Exercises SET imagePath = 'mahi_nog.webp' WHERE ExercisesName = 'Махи ногами'",
            "UPDATE Exercises SET imagePath = 'heel_touches.webp' WHERE ExercisesName = 'Касание пяток лежа на спине'",

            "UPDATE Exercises SET imagePath = 'run_dorozka.webp' WHERE ExercisesName = 'Беговая дорожка'",
            "UPDATE Exercises SET imagePath = 'elliptical.webp' WHERE ExercisesName = 'Эллипсоид'",
            "UPDATE Exercises SET imagePath = 'exercise_bike.webp' WHERE ExercisesName = 'Велосипед'",
            "UPDATE Exercises SET imagePath = 'elliptical.webp' WHERE ExercisesName = 'Степпер'",
            "UPDATE Exercises SET imagePath = 'skakalka.webp' WHERE ExercisesName = 'Скакалка'",
            "UPDATE Exercises SET imagePath = 'greblya.webp' WHERE ExercisesName = 'Гребля'",
            "UPDATE Exercises SET imagePath = 'skalolaz.webp' WHERE ExercisesName = 'Скалолаз'",
            "UPDATE Exercises SET imagePath = 'run_with_high_leg.webp' WHERE ExercisesName = 'Бег с высоким поднимание ног'",
            "UPDATE Exercises SET imagePath = 'run_with_high_leg.webp' WHERE ExercisesName = 'Захлест голени'"
        )

        updates.forEach { db.execSQL(it) }
    }
}

val MIGRATION_10_11=object : Migration(10,11){
    override fun migrate(db: SupportSQLiteDatabase) {
        val updates = listOf(
            "UPDATE Exercises SET imagePath = 'skruchivanie.webp' WHERE ExercisesName = 'Скручивания'",
            "UPDATE Exercises SET imagePath = 'zhim_uzky_tryceps.webp' WHERE ExercisesName = 'Жим лёжа узким хватом'",
            "UPDATE Exercises SET imagePath = 'podtyg.webp' WHERE ExercisesName = 'Подтягивания'",
            "UPDATE Exercises SET imagePath = 'razvedenye_ruk.webp' WHERE ExercisesName = 'Разведение рук'",
            "UPDATE Exercises SET imagePath = 'zhim_iz_za_golov.webp' WHERE ExercisesName = 'Жим из за головы'",
            "UPDATE Exercises SET imagePath = 'velosiped_press.webp' WHERE ExercisesName = 'Велосипед для пресса'"
            )

        updates.forEach { db.execSQL(it) }
    }

}

val MIGRATION_11_12=object : Migration(11,12){
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE Exercises SET imagePath = 'zhim_lezha.webp' WHERE ExercisesName = 'Жим лежа'")
        db.execSQL("UPDATE Exercises SET imagePath = 'otzhimanya.webp' WHERE ExercisesName = 'Отжимания'")
        db.execSQL("UPDATE Exercises SET imagePath = 'velosiped_press.webp' WHERE ExercisesName = 'Велосипед для пресса'")
    }

}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.run {
            execSQL("""
                CREATE TABLE IF NOT EXISTS ExerciseInfo (
                    infoid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    exerciseId INTEGER NOT NULL,
                    description TEXT NOT NULL,
                    executionTips TEXT NOT NULL,
                    FOREIGN KEY (exerciseId) REFERENCES Exercises(ExercisesId) ON DELETE CASCADE
                )
            """.trimIndent())

            execSQL("CREATE INDEX IF NOT EXISTS index_ExerciseInfo_exerciseId ON ExerciseInfo(exerciseId)")

            execSQL("""
                CREATE TABLE IF NOT EXISTS ExercisesStats (
                    statid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    exerciseId INTEGER NOT NULL,
                    totalSets INTEGER NOT NULL,
                    totalResp INTEGER NOT NULL,
                    totalWeight INTEGER NOT NULL,
                    totalTrainings INTEGER NOT NULL,
                    maxWeight INTEGER NOT NULL,
                    maxResp INTEGER NOT NULL,
                    maxWorkoutVolume INTEGER NOT NULL,
                    maxWorkoutVolumeDate TEXT NOT NULL,
                    estimatedOneRepMax REAL NOT NULL,
                    oneRepMaxDate TEXT NOT NULL,
                    FOREIGN KEY (exerciseId) REFERENCES Exercises(ExercisesId) ON DELETE CASCADE
                )
            """.trimIndent())

            execSQL("CREATE INDEX IF NOT EXISTS index_ExercisesStats_exerciseId ON ExercisesStats(exerciseId)")

            execSQL("UPDATE Exercises SET imagePath = 'tyaga_lezha_naklon.webp' WHERE ExercisesName = 'Тяга лёжа'")
            execSQL("UPDATE Exercises SET ExercisesName = 'Бег с высоким подниманием ног' WHERE ExercisesName = 'Бег с высоким подниманием ног'")
        }
    }
}
val MIGRATION_13_14 = object : Migration(13,14){
    override fun migrate(db: SupportSQLiteDatabase) {
        db.run {
            execSQL("""
                CREATE TABLE IF NOT EXISTS GeneralTrainingStats(
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                totalTrainings INTEGER NOT NULL,
                trainingDays INTEGER NOT NULL,
                trainingWeeks INTEGER NOT NULL,
                totalVolume INTEGER NOT NULL,
                totalDistance INTEGER NOT NULL,
                totalExercises INTEGER NOT NULL,
                totalSets INTEGER NOT NULL,
                totalReps INTEGER NOT NULL
                )
            """.trimIndent())

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS BodyMeasurements (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                neck INTEGER NOT NULL,
                shoulders INTEGER NOT NULL,
                forearms INTEGER NOT NULL,
                biceps INTEGER NOT NULL,
                chest INTEGER NOT NULL,
                waist INTEGER NOT NULL,
                triceps INTEGER NOT NULL,
                pelvis INTEGER NOT NULL
                )   
            """.trimIndent())
        }
    }

}
val MIGRATION_14_15 = object : Migration(14,15){
    override fun migrate(db: SupportSQLiteDatabase) {
        db.run {
            db.execSQL("Drop table if exists BodyMeasurements")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS BodyMeasurements (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    date TEXT NOT NULL,
                    neck INTEGER NOT NULL,
                    shoulders INTEGER NOT NULL,
                    chest INTEGER NOT NULL,
                    waist INTEGER NOT NULL,
                    pelvis INTEGER NOT NULL,
                    bedroLeft INTEGER NOT NULL,
                    ikriLeft INTEGER NOT NULL,
                    begroRight INTEGER NOT NULL,
                    ikriRight INTEGER NOT NULL,
                    forearmsLeft INTEGER NOT NULL,
                    forearmsRight INTEGER NOT NULL,
                    bicepsLeft INTEGER NOT NULL,
                    bicepsRight INTEGER NOT NULL,
                    tricepsLeft INTEGER NOT NULL,
                    tricepsRight INTEGER NOT NULL
                )   
            """.trimIndent())
        }
    }

}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.run {
            // Удаляем старую таблицу, если есть
            execSQL("DROP TABLE IF EXISTS BodyMeasurements")

            // Создаём новую таблицу с nullable полями (без NOT NULL)
            execSQL(
                """
                CREATE TABLE IF NOT EXISTS BodyMeasurements (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    date TEXT NOT NULL,
                    neck INTEGER,
                    shoulders INTEGER,
                    chest INTEGER,
                    waist INTEGER,
                    pelvis INTEGER,
                    bedroLeft INTEGER,
                    ikriLeft INTEGER,
                    begroRight INTEGER,
                    ikriRight INTEGER,
                    forearmsLeft INTEGER,
                    forearmsRight INTEGER,
                    bicepsLeft INTEGER,
                    bicepsRight INTEGER,
                    tricepsLeft INTEGER,
                    tricepsRight INTEGER
                )
                """.trimIndent()
            )
        }
    }
}
val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Sets ADD COLUMN minutes INTEGER")
        db.execSQL("ALTER TABLE Sets ADD COLUMN seconds INTEGER")
        db.execSQL("ALTER TABLE Sets ADD COLUMN distanceKm REAL")
    }
}
val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Exercises ADD COLUMN type TEXT NOT NULL DEFAULT 'STRENGTH'")
        // Устанавливаем типы для кардио упражнений (по ID или Имени)
        db.execSQL("UPDATE Exercises SET type = 'CARDIO_DISTANCE' WHERE ExercisesName IN ('Беговая дорожка', 'Эллипсоид', 'Велосипед', 'Степпер', 'Гребля')")
        db.execSQL("UPDATE Exercises SET type = 'CARDIO_TIME_REPS' WHERE ExercisesName IN ('Скакалка', 'Скалолаз', 'Бег с высоким подниманием ног', 'Захлест голени')")

    }
}
val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "UPDATE Exercises SET ExercisesName = 'Бег с высоким подниманием ног' WHERE ExercisesName = 'Бег с высоким поднимание ног'"
        )
    }
}
val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Создать новую таблицу без duration
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS Sets_temp (
                setId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                trainingId INTEGER NOT NULL,
                exerciseId INTEGER NOT NULL,
                reps INTEGER,
                weight INTEGER,
                minutes INTEGER,
                seconds INTEGER,
                distanceKm REAL,
                exerciseOrder INTEGER NOT NULL,
                FOREIGN KEY(trainingId) REFERENCES Trainings(trainingId) ON DELETE CASCADE,
                FOREIGN KEY(exerciseId) REFERENCES Exercises(ExercisesId) ON DELETE CASCADE
            )
        """.trimIndent())

        // 2. Скопировать данные (только нужные колонки)
        db.execSQL("""
            INSERT INTO Sets_temp (setId, trainingId, exerciseId, reps, weight, minutes, seconds, distanceKm, exerciseOrder)
            SELECT setId, trainingId, exerciseId, reps, weight, minutes, seconds, distanceKm, exerciseOrder
            FROM Sets
        """.trimIndent())

        // 3. Удалить старую таблицу
        db.execSQL("DROP TABLE Sets")

        // 4. Переименовать новую в Sets
        db.execSQL("ALTER TABLE Sets_temp RENAME TO Sets")

        // 5. Восстановить индексы, если были
        db.execSQL("CREATE INDEX IF NOT EXISTS index_Sets_exerciseId ON Sets(exerciseId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_Sets_trainingId ON Sets(trainingId)")
    }
}










