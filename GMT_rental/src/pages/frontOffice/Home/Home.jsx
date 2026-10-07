import "./Home.css";


import VehicleSelector from "../../../components/frontoffice/VehicleSelector/VehicleSelector";
import VehiclesSection from "../../../components/frontoffice/VehiclesSection/VehiclesSection";
import AdvantagesSection from "../../../components/frontoffice/AdvantagesSection/AdvantagesSection";
import CTASection from "../../../components/frontoffice/CTASection/CTASection";

import { NavLink } from "react-router-dom";

function Home() {
  return (
    <main>

      {/* HERO */}
      <section className="hero">

        <div className="hero-overlay"></div>

        <div className="hero-content">

          <p className="hero-badge">
            <span>●</span> Location premium en Tunisie
          </p>

          <h1>
            L'art du
            <br />
            voyage
            <br />
            <span>tunisien</span>
          </h1>

          <p className="hero-description">
            Explorez la Tunisie au volant de véhicules d'exception.
            Un service sur mesure, des voitures premium, et la liberté
            de la route méditerranéenne.
          </p>

          <div className="hero-buttons">

            <NavLink
              to="/devis"
              className="hero-button-primary"
            >
              Réserver →
            </NavLink>

            <NavLink
              to="/devis"
              className="hero-button-secondary"
            >
              Demander un devis
            </NavLink>

          </div>

        </div>

      </section>

      {/* SELECTEUR DE VEHICULE */}
      <VehicleSelector />

      {/* VEHICULES */}
      <VehiclesSection />

      {/* AVANTAGES */}
      <AdvantagesSection />

      {/* CTA FINAL */}
      <CTASection />

    </main>
  );
}

export default Home;
