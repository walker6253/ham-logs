import 'package:drift/drift.dart';
import 'tables.dart';
import 'contact_dao.dart';
import 'daily_log_dao.dart';
import 'database_connection.dart';
part 'app_database.g.dart';

@DriftDatabase(tables: [ContactRecords, DailyLogs], daos: [ContactDao, DailyLogDao])
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(createConnection());
  @override
  int get schemaVersion => 1;
}