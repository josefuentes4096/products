import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./contexts/AuthContext";
import { CarritoProvider } from "./contexts/CarritoContext";

import NavBar from "./components/NavBar";
import Footer from "./components/Footer";

import Home from "./pages/Home";
import Login from "./pages/Login";
import Cart from "./components/Cart";
import ProductDetail from "./pages/ProductDetail";
import Admin from "./components/Admin";
import Administracion from "./pages/Administracion";
import GestionProductos from "./pages/GestionProductos";
import GestionCategorias from "./pages/GestionCategorias";
import BuscarProducto from "./pages/BuscarProducto";
import HistorialPedidos from "./pages/HistorialPedidos";
import FormularioProducto from "./components/FormularioProducto";
import FormularioEdicion from "./components/FormularioEdicion";
import ProtectedRoute from "./components/ProtectedRoute";

function App() {
  return (
    <AuthProvider>
      <CarritoProvider>
        <BrowserRouter basename="/mi-ecommerce">
          <div className="d-flex flex-column min-vh-100">
            <NavBar />
            <main className="container flex-grow-1 py-4">
              <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/category/:slug" element={<Home />} />
                <Route path="/product/:id" element={<ProductDetail />} />
                <Route path="/login" element={<Login />} />

                <Route path="/gestion-productos" element={<GestionProductos />} />
                <Route path="/categorias" element={<GestionCategorias />} />
                <Route path="/buscar" element={<BuscarProducto />} />

                <Route path="/cart" element={
                  <ProtectedRoute><Cart /></ProtectedRoute>
                } />
                <Route path="/historial" element={
                  <ProtectedRoute><HistorialPedidos /></ProtectedRoute>
                } />
                <Route path="/admin" element={
                  <ProtectedRoute><Administracion /></ProtectedRoute>
                } />
                <Route path="/admin/productos" element={
                  <ProtectedRoute><Admin /></ProtectedRoute>
                } />
                <Route path="/admin/agregarProducto" element={
                  <ProtectedRoute><FormularioProducto /></ProtectedRoute>
                } />
                <Route path="/admin/editarProducto/:id" element={
                  <ProtectedRoute><FormularioEdicion /></ProtectedRoute>
                } />

                <Route path="*" element={<Navigate to="/" />} />
              </Routes>
            </main>
            <Footer />
          </div>
        </BrowserRouter>
      </CarritoProvider>
    </AuthProvider>
  );
}

export default App;
