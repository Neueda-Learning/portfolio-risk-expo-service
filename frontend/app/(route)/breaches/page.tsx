export const dynamic = "force-dynamic";

import { getPortfolios } from "@/lib/api";
import { getBreaches } from "@/lib/api/breaches";
import { BreachesPage } from "@/components/breaches/BreachesPage";

export default async function BreachesRoute() {
  const [portfolios, allBreaches] = await Promise.all([
    getPortfolios(),
    getBreaches(),
  ]);

  return <BreachesPage allBreaches={allBreaches} portfolios={portfolios} />;
}
