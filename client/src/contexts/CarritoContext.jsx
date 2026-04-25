import { createContext, useContext, useState } from "react";
import client from "../api/client";

export const CarritoContext = createContext();

export function CarritoProvider({ children }) {
  const [productos, setProductos] = useState([]);
  const [pedidoError, setPedidoError] = useState(null);
  const [pedidoExitoso, setPedidoExitoso] = useState(false);

  const agregarProducto = (producto) => {
    setProductos((prev) => {
      const existe = prev.find((p) => p.id === producto.id);
      if (existe) {
        return prev.map((p) =>
          p.id === producto.id ? { ...p, cantidad: p.cantidad + 1 } : p
        );
      }
      return [...prev, { ...producto, cantidad: 1 }];
    });
  };

  const disminuirProducto = (id) => {
    setProductos((prev) =>
      prev
        .map((p) => p.id === id ? { ...p, cantidad: p.cantidad - 1 } : p)
        .filter((p) => p.cantidad > 0)
    );
  };

  const eliminarProducto = (id) => {
    setProductos((prev) => prev.filter((p) => p.id !== id));
  };

  const vaciarCarrito = () => {
    setProductos([]);
  };

  const confirmarPedido = async (userId = 1) => {
    setPedidoError(null);
    setPedidoExitoso(false);
    try {
      await client.post("/orders", {
        userId,
        items: productos.map((p) => ({
          productId: p.id,
          quantity: p.cantidad,
        })),
      });
      vaciarCarrito();
      setPedidoExitoso(true);
      setTimeout(() => setPedidoExitoso(false), 4000);
    } catch {
      setPedidoError("No se pudo confirmar el pedido. Intente nuevamente.");
    }
  };

  return (
    <CarritoContext.Provider
      value={{ productos, agregarProducto, disminuirProducto, eliminarProducto, vaciarCarrito, confirmarPedido, pedidoError, pedidoExitoso }}
    >
      {children}
    </CarritoContext.Provider>
  );
}

export const useCarrito = () => useContext(CarritoContext);
