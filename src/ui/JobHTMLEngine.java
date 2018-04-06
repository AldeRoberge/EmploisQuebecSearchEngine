package ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class JobHTMLEngine implements Runnable {

	private Job account;
	private static Loading progressBar;

	public JobHTMLEngine(Job account, Loading run) {
		this.account = account;
		this.progressBar = run;
	}

	public static int current = 0;
	public static int total = 0;

	public static ArrayList<Job> waitingForAnswer;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void verifyEmplois(List<Job> accountList, int amountOfThreads, Loading load) throws ExecutionException, InterruptedException {

		current = 0;
		total = 0;

		waitingForAnswer = new ArrayList<Job>();

		total = accountList.size();

		ExecutorService service = Executors.newFixedThreadPool(amountOfThreads);
		List<Future<Runnable>> futures = new ArrayList<Future<Runnable>>();

		for (Job a : accountList) {
			Future f = service.submit(new JobHTMLEngine(a, load));
			futures.add(f);
			waitingForAnswer.add(a);
		}

		for (Future<Runnable> f : futures) {
			f.get();
		}

		progressBar.reportStatus("Ended.");

		Thread.sleep(400);

		service.shutdownNow();

		load.reportEnded(accountList);

	}

	public void run() {

		Job a = account;

		try {

			URL page;

			page = new URL(a.url);

			BufferedReader in = new BufferedReader(new InputStreamReader(page.openStream()));

			String inputLine;

			boolean isInsideJobOfferings = false;

			StringBuilder jobsRaw = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {

				if (inputLine.contains("<h1>Visualisation de l'offre d'emploi")) {

					progressBar.reportStatus("Loading job listing " + current + "...");
					isInsideJobOfferings = true;

				} else if (inputLine.contains("LISTE DES OFFRES")) {

					progressBar.reportStatus("Successfully loaded job listing " + current + "...");
					isInsideJobOfferings = false;

					break;
				}

				if (isInsideJobOfferings) {
					jobsRaw.append(inputLine);
				}

			}

			in.close();

			a.html = jobsRaw.toString();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		current++;
		System.out.println(current + "\\" + total);

		progressBar.reportTotal(current, total);
		waitingForAnswer.remove(account);

	}

}