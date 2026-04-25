import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import ProductDetail from '../pages/ProductDetail'

vi.mock('../api/client', () => ({
  default: { get: vi.fn() },
}))

const mockAgregarProducto = vi.fn()
const mockNavigate = vi.fn()
let mockUser = 'jose'

vi.mock('../contexts/CarritoContext', () => ({
  useCarrito: () => ({ agregarProducto: mockAgregarProducto }),
}))

vi.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({ user: mockUser }),
}))

vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal()
  return { ...actual, useNavigate: () => mockNavigate }
})

import client from '../api/client'

const strat = {
  id: 1,
  name: 'Fender Stratocaster American Pro II',
  price: 1500,
  category: 'Guitarras',
  description: 'Guitarra electrica con pastillas V-Mod II',
  stock: 8,
  imageUrl: 'strat.jpg',
}

beforeEach(() => {
  vi.clearAllMocks()
  mockUser = 'jose'
})

function renderDetail(id = '1') {
  return render(
    <MemoryRouter initialEntries={[`/product/${id}`]}>
      <Routes>
        <Route path="/product/:id" element={<ProductDetail />} />
      </Routes>
    </MemoryRouter>
  )
}

// -------------------------------------------------------------------------
// Visualización del producto
// -------------------------------------------------------------------------

test('muestra los datos del producto al cargar', async () => {
  client.get.mockResolvedValueOnce({ data: strat })

  renderDetail()

  await waitFor(() => {
    expect(screen.getByText('Fender Stratocaster American Pro II')).toBeInTheDocument()
    expect(screen.getByText(/\$1500/)).toBeInTheDocument()
    expect(screen.getByText('Guitarras')).toBeInTheDocument()
    expect(screen.getByText('Guitarra electrica con pastillas V-Mod II')).toBeInTheDocument()
  })
})

test('muestra error si la API falla al cargar el producto', async () => {
  client.get.mockRejectedValueOnce(new Error('Not found'))

  renderDetail()

  await waitFor(() => {
    expect(screen.getByText(/no se pudo cargar el producto/i)).toBeInTheDocument()
  })
})

// -------------------------------------------------------------------------
// Agregar al carrito
// -------------------------------------------------------------------------

test('agrega al carrito y navega a /cart cuando el usuario está autenticado', async () => {
  client.get.mockResolvedValueOnce({ data: strat })

  renderDetail()

  await waitFor(() => screen.getByRole('button', { name: /agregar al carrito/i }))

  await userEvent.click(screen.getByRole('button', { name: /agregar al carrito/i }))

  expect(mockAgregarProducto).toHaveBeenCalledWith(strat)
  expect(mockNavigate).toHaveBeenCalledWith('/cart')
})

test('redirige a /login si el usuario no está autenticado', async () => {
  mockUser = null
  client.get.mockResolvedValueOnce({ data: strat })

  renderDetail()

  await waitFor(() => screen.getByRole('button', { name: /agregar al carrito/i }))

  await userEvent.click(screen.getByRole('button', { name: /agregar al carrito/i }))

  expect(mockAgregarProducto).not.toHaveBeenCalled()
  expect(mockNavigate).toHaveBeenCalledWith('/login')
})
