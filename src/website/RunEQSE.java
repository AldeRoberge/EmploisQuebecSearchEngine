package website;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import api.EmploiQuebecAPI;
import api.region.RegionDTO;
import api.region.city.CityDTO;
import api.region.city.job.JobDTO;
import io.javalin.Javalin;

public class RunEQSE {

	private static EmploiQuebecAPI emploiQuebecAPI = new EmploiQuebecAPI();

	public static void main(String[] args) {
		new RunEQSE();
	}

	public RunEQSE() {
		Javalin app = Javalin.create();

		app.enableCorsForAllOrigins();

		StringBuilder explanation = new StringBuilder();
		explanation.append("Bienvenue sur EmploisQuebecSearchEngine API!" + "\n\n");
		explanation.append("Si vous souhaitez accedez à l'interface utilisateur, veuillez installer Angular CLI sur votre machine," + "\n");
		explanation.append("puis utiliser la commande 'ng serve' dans le dossier 'webapp' de cette source." + "\n\n");
		explanation.append("C'est tout! Visitez localhost:4200 sur votre machine pour voir le site local." + "\n");

		app.get("/", ctx -> ctx.result(explanation.toString()));

		app.get("/regions", ctx -> ctx.json(getRegions())); // Get all regions

		app.get("/cities/:region-code", ctx -> {
			ctx.json(getCities(ctx.pathParam("region-code"))); // Get all cities for region
		});

		app.get("/jobs/:city-url", ctx -> {
			ctx.json(getJobs(ctx.pathParam("city-url"))); // Get all jobs for city
		});

		app.start();
	}

	/**
	 * Regions
	 */
	List<RegionDTO> cachedRegions = new ArrayList<>();

	public List<RegionDTO> getRegions() {
		if (cachedRegions.isEmpty()) {
			cachedRegions.addAll(emploiQuebecAPI.getRegions());
		}

		return cachedRegions;
	}

	/**
	 * Cities
	 */
	Map<String, List<CityDTO>> cachedCities = new HashMap<>();

	public List<CityDTO> getCities(String regionCode) {
		if (cachedCities.get(regionCode) == null) {
			cachedCities.put(regionCode, emploiQuebecAPI.getCities(new RegionDTO(regionCode)));
		}
		return cachedCities.get(regionCode);
	}

	/**
	 * Jobs
	 */
	Map<String, List<JobDTO>> cachedJobs = new HashMap<>();

	public List<JobDTO> getJobs(String cityUrl) {
		if (cachedJobs.get(cityUrl) == null) {
			cachedJobs.put(cityUrl, emploiQuebecAPI.getJobs(new CityDTO(cityUrl)));
		}

		return cachedJobs.get(cityUrl);
	}

}