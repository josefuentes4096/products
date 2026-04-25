import { useAuth } from '../contexts/AuthContext';
import { useCarrito } from '../contexts/CarritoContext';
import { useNavigate, Link } from 'react-router-dom';

export default function ProductCard({ producto, onEdit, onDelete, modo }) {
  const { user } = useAuth();
  const { agregarProducto } = useCarrito();
  const navigate = useNavigate();

  const handleAgregar = () => {
    if (user) {
      agregarProducto(producto);
    } else {
      navigate("/login");
    }
  };

  return (
    <div className="card h-100">
      <Link to={`/product/${producto.id}`}>
        <img
          src={producto.imageUrl}
          alt={producto.name}
          className="card-img-top"
          style={{ objectFit: 'cover', height: '200px' }}
        />
      </Link>
      <div className="card-body d-flex flex-column justify-content-between">
        <div>
          <Link to={`/product/${producto.id}`} className="text-decoration-none text-dark">
            <h5 className="card-title">{producto.name}</h5>
          </Link>
          <p className="card-text text-muted small">{producto.description}</p>
          <p className="fw-bold">${producto.price}</p>
        </div>

        {modo === "admin" ? (
          <div className="d-flex justify-content-between mt-3">
            <button className="btn btn-warning btn-sm" onClick={() => onEdit(producto)}>Editar</button>
            <button className="btn btn-danger btn-sm" onClick={() => onDelete(producto.id)}>Eliminar</button>
          </div>
        ) : (
          <button className="btn btn-primary mt-3" onClick={handleAgregar}>
            Agregar al carrito
          </button>
        )}
      </div>
    </div>
  );
}
