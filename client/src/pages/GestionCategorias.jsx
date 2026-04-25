import { Link } from "react-router-dom";

const CATEGORIAS = [
  { nombre: "Guitarras",      descripcion: "Guitarras eléctricas y acústicas",            icono: "🎸" },
  { nombre: "Pedales",        descripcion: "Pedales de efecto y procesadores de señal",    icono: "🎛️" },
  { nombre: "Amplificadores", descripcion: "Amplificadores valvulares y de estado sólido", icono: "🔊" },
];

export default function GestionCategorias() {
  return (
    <div className="container py-4">
      <h4 className="mb-1">Gestión de Categorías</h4>
      <p className="text-muted mb-4">
        Explorá los productos por categoría. La creación y edición de categorías no está disponible en esta versión.
      </p>
      <div className="row g-3">
        {CATEGORIAS.map((cat) => (
          <div key={cat.nombre} className="col-md-4">
            <div className="card h-100 text-center p-3 shadow-sm">
              <div style={{ fontSize: "3rem" }}>{cat.icono}</div>
              <h5 className="mt-2">{cat.nombre}</h5>
              <p className="text-muted">{cat.descripcion}</p>
              <Link to={`/category/${cat.nombre}`} className="btn btn-outline-primary btn-sm mt-auto">
                Ver productos
              </Link>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
