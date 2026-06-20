package com.example.ecostay.data;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

final class DatabaseMigrations {

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS `support_messages`");
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `support_messages` (" +
                            "`messageId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`userId` INTEGER NOT NULL, " +
                            "`userName` TEXT NOT NULL, " +
                            "`message` TEXT NOT NULL, " +
                            "`createdAt` TEXT NOT NULL" +
                            ")"
            );
            db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_support_messages_userId` " +
                            "ON `support_messages` (`userId`)"
            );
        }
    };

    private DatabaseMigrations() {
    }
}
