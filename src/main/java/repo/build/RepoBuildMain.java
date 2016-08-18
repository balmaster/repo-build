package repo.build;

import repo.build.RepoBuild;

// почемуто потребовался именно java класс, для того чтобы maven exec:java его подхватывал
public class RepoBuildMain {

    public static void main(String[] args) {
        RepoBuild.main(args);
    }

}
