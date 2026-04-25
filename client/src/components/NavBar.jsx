import { Link, useNavigate } from "react-router-dom";
import { useCarrito } from "../contexts/CarritoContext";
import { useAuth } from "../contexts/AuthContext";

function NavBar() {
  const { productos } = useCarrito();
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <header className="bg-dark border-bottom shadow-sm">
      <div className="container">
        <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
          <Link className="navbar-brand fw-bold" to="/">TechLab</Link>
          <button
            className="navbar-toggler"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#mainNav"
          >
            <span className="navbar-toggler-icon"></span>
          </button>
          <div className="collapse navbar-collapse" id="mainNav">
            <ul className="navbar-nav me-auto mb-2 mb-lg-0">
              <li className="nav-item">
                <Link className="nav-link" to="/gestion-productos">1. Productos</Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="/categorias">2. Categorías</Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="/cart">3. Carrito ({productos?.length || 0})</Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="/cart">4. Realizar Pedido</Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="/historial">5. Historial</Link>
              </li>
              {user && (
                <li className="nav-item">
                  <Link className="nav-link" to="/admin">6. Administración</Link>
                </li>
              )}
            </ul>
            <div className="d-flex gap-2 align-items-center">
              {user ? (
                <>
                  <span className="text-light small">Hola, {user}</span>
                  <button onClick={handleLogout} className="btn btn-outline-light btn-sm">
                    7. Salir
                  </button>
                </>
              ) : (
                <Link to="/login" className="btn btn-outline-light btn-sm">Login</Link>
              )}
            </div>
          </div>
        </nav>
      </div>
    </header>
  );
}

export default NavBar;
