public class Main {
    private final static String DATABASE_NAME = "fitnesscenter";

    public static void main(String[] args) {
        Main main = new Main();
        main.run();
    }

    public void run() {
        ReactiveMongoDriver reactiveMongoDriver = new ReactiveMongoDriver(DATABASE_NAME);
        TurnstileServer turnstileServerServer = new TurnstileServer(reactiveMongoDriver);
        ManagerServer managerServer = new ManagerServer(reactiveMongoDriver);
        ReportServer reportServer = new ReportServer(reactiveMongoDriver.getEvents());

        new Thread(new ManagerServerRunner(managerServer)).start();
        new Thread(new TurnstileServerRunner(turnstileServerServer)).start();
        new Thread(new ReportServerRunner(reportServer)).start();
    }

    public static class TurnstileServerRunner implements Runnable {

        private final TurnstileServer entryServer;

        public TurnstileServerRunner(TurnstileServer entryServer) {
            this.entryServer = entryServer;
        }

        @Override
        public void run() {
            entryServer.run();
        }
    }

    public static class ManagerServerRunner implements Runnable {

        private final ManagerServer managerServer;

        public ManagerServerRunner(ManagerServer managerServer) {
            this.managerServer = managerServer;
        }

        @Override
        public void run() {
            managerServer.run();
        }
    }

    public static class ReportServerRunner implements Runnable {

        private final ReportServer reportServer;

        public ReportServerRunner(ReportServer reportServer) {
            this.reportServer = reportServer;
        }

        @Override
        public void run() {
            reportServer.run();
        }
    }
}