package dev.diegoh.sumo.stats;

public enum DatabaseDriver {
  SQLITE("org.sqlite.JDBC"),
  MYSQL("org.mariadb.jdbc.Driver");

  private final String driverClass;

  DatabaseDriver(String driverClass) {
    this.driverClass = driverClass;
  }

  public String driverClass() {
    return driverClass;
  }
}
