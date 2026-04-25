import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import ProductList from '../components/ProductList'

vi.mock('../api/client', () => ({
  default: { get: vi.fn(), delete: vi.fn() },
}))

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal()
  return { ...actual, useNavigate: () => mockNavigate }
})

// Stub de ProductCard para evitar dependencias del contexto
vi.mock('../components/ProductCard', () => ({
  default: ({ producto, onDelete, onEdit }) => (
    <div data-testid={`card-${producto.id}`}>
      <span>{producto.name}</span>
      <button onClick={() => onDelete(producto.id)}>Eliminar</button>
      <button onClick={() => onEdit(producto)}>Editar</button>
    </div>
  ),
}))

import client from '../api/client'

const guitarras = [
  { id: 1, name: 'Fender Stratocaster', price: 1500, category: 'Guitarras', stock: 8, imageUrl: '' },
  { id: 2, name: 'Gibson Les Paul',     price: 2000, category: 'Guitarras', stock: 3, imageUrl: '' },
]

beforeEach(() => vi.clearAllMocks())

function renderList(category) {
  return render(
    <MemoryRouter>
      <ProductList category={category} />
    </MemoryRouter>
  )
}

// -------------------------------------------------------------------------
// Carga de productos
// -------------------------------------------------------------------------

test('carga y muestra todos los productos desde /products', async () => {
  client.get.mockResolvedValueOnce({ data: { content: guitarras } })

  renderList()

  await waitFor(() => {
    expect(client.get).toHaveBeenCalledWith('/products')
    expect(screen.getByText('Fender Stratocaster')).toBeInTheDocument()
    expect(screen.getByText('Gibson Les Paul')).toBeInTheDocument()
  })
})

test('filtra por categoría usando /products/category/:cat', async () => {
  client.get.mockResolvedValueOnce({ data: { content: guitarras } })

  renderList('Guitarras')

  await waitFor(() => {
    expect(client.get).toHaveBeenCalledWith('/products/category/Guitarras')
  })
})

test('muestra mensaje cuando no hay productos disponibles', async () => {
  client.get.mockResolvedValueOnce({ data: { content: [] } })

  renderList()

  await waitFor(() => {
    expect(screen.getByText(/no hay productos disponibles/i)).toBeInTheDocument()
  })
})

test('muestra error si la API falla al cargar', async () => {
  client.get.mockRejectedValueOnce(new Error('Network error'))

  renderList()

  await waitFor(() => {
    expect(screen.getByText(/error al cargar productos/i)).toBeInTheDocument()
  })
})

// -------------------------------------------------------------------------
// Eliminar producto
// -------------------------------------------------------------------------

test('elimina el producto al confirmar el diálogo', async () => {
  client.get.mockResolvedValueOnce({ data: { content: guitarras } })
  client.delete.mockResolvedValueOnce({})
  vi.spyOn(window, 'confirm').mockReturnValue(true)

  renderList()

  await waitFor(() => screen.getByTestId('card-1'))

  await userEvent.click(screen.getAllByRole('button', { name: /eliminar/i })[0])

  await waitFor(() => {
    expect(client.delete).toHaveBeenCalledWith('/products/1')
    expect(screen.queryByTestId('card-1')).not.toBeInTheDocument()
  })
})

test('no elimina el producto si el usuario cancela el diálogo', async () => {
  client.get.mockResolvedValueOnce({ data: { content: guitarras } })
  vi.spyOn(window, 'confirm').mockReturnValue(false)

  renderList()

  await waitFor(() => screen.getByTestId('card-1'))

  await userEvent.click(screen.getAllByRole('button', { name: /eliminar/i })[0])

  expect(client.delete).not.toHaveBeenCalled()
  expect(screen.getByTestId('card-1')).toBeInTheDocument()
})

// -------------------------------------------------------------------------
// Editar producto
// -------------------------------------------------------------------------

test('navega a la página de edición al hacer clic en editar', async () => {
  client.get.mockResolvedValueOnce({ data: { content: guitarras } })

  renderList()

  await waitFor(() => screen.getByTestId('card-1'))

  await userEvent.click(screen.getAllByRole('button', { name: /editar/i })[0])

  expect(mockNavigate).toHaveBeenCalledWith('/admin/editarProducto/1')
})
