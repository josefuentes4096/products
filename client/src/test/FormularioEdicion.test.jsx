import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import FormularioEdicion from '../components/FormularioEdicion'

vi.mock('../api/client', () => ({
  default: { get: vi.fn(), put: vi.fn() },
}))

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal()
  return { ...actual, useNavigate: () => mockNavigate }
})

import client from '../api/client'

const strat = {
  id: 3,
  name: 'Fender Stratocaster',
  price: 1500,
  description: 'Guitarra electrica clasica americana',
  category: 'Guitarras',
  stock: 5,
  imageUrl: 'strat.jpg',
}

beforeEach(() => vi.clearAllMocks())

function renderEdicion(id = '3') {
  return render(
    <MemoryRouter initialEntries={[`/admin/editarProducto/${id}`]}>
      <Routes>
        <Route path="/admin/editarProducto/:id" element={<FormularioEdicion />} />
      </Routes>
    </MemoryRouter>
  )
}

// -------------------------------------------------------------------------
// Carga del producto
// -------------------------------------------------------------------------

test('muestra los datos del producto en el formulario al cargar', async () => {
  client.get.mockResolvedValueOnce({ data: strat })

  renderEdicion()

  await waitFor(() => {
    expect(screen.getByDisplayValue('Fender Stratocaster')).toBeInTheDocument()
    expect(screen.getByDisplayValue('1500')).toBeInTheDocument()
    expect(screen.getByDisplayValue('Guitarras')).toBeInTheDocument()
  })
})

test('muestra error si no se puede cargar el producto', async () => {
  client.get.mockRejectedValueOnce(new Error('Not found'))

  renderEdicion()

  await waitFor(() => {
    expect(screen.getByText(/no se pudo cargar/i)).toBeInTheDocument()
  })
})

// -------------------------------------------------------------------------
// Validaciones
// -------------------------------------------------------------------------

test('muestra error si el nombre queda vacío al guardar', async () => {
  client.get.mockResolvedValueOnce({ data: strat })

  renderEdicion()

  await waitFor(() => screen.getByDisplayValue('Fender Stratocaster'))

  await userEvent.clear(screen.getByDisplayValue('Fender Stratocaster'))
  await userEvent.click(screen.getByRole('button', { name: /guardar cambios/i }))

  expect(screen.getByText(/complete todos los campos/i)).toBeInTheDocument()
  expect(client.put).not.toHaveBeenCalled()
})

test('muestra error si la descripción tiene menos de 10 caracteres', async () => {
  client.get.mockResolvedValueOnce({ data: strat })

  renderEdicion()

  await waitFor(() => screen.getByDisplayValue('Guitarra electrica clasica americana'))

  await userEvent.clear(screen.getByDisplayValue('Guitarra electrica clasica americana'))
  await userEvent.type(screen.getByDisplayValue(''), 'Corta')
  await userEvent.click(screen.getByRole('button', { name: /guardar cambios/i }))

  expect(screen.getByText(/complete todos los campos/i)).toBeInTheDocument()
})

// -------------------------------------------------------------------------
// Envío exitoso
// -------------------------------------------------------------------------

test('llama a PUT /products/:id con los datos correctos al guardar', async () => {
  client.get.mockResolvedValueOnce({ data: strat })
  client.put.mockResolvedValueOnce({})

  renderEdicion()

  await waitFor(() => screen.getByDisplayValue('Fender Stratocaster'))

  await userEvent.click(screen.getByRole('button', { name: /guardar cambios/i }))

  await waitFor(() => {
    expect(client.put).toHaveBeenCalledWith('/products/3', {
      name: 'Fender Stratocaster',
      price: 1500,
      description: 'Guitarra electrica clasica americana',
      category: 'Guitarras',
      stock: 5,
      imageUrl: 'strat.jpg',
    })
  })
})

test('redirige a /admin tras guardar exitosamente', async () => {
  client.get.mockResolvedValueOnce({ data: strat })
  client.put.mockResolvedValueOnce({})

  renderEdicion()

  await waitFor(() => screen.getByDisplayValue('Fender Stratocaster'))

  await userEvent.click(screen.getByRole('button', { name: /guardar cambios/i }))

  await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/admin'))
})

test('muestra error si la API falla al actualizar', async () => {
  client.get.mockResolvedValueOnce({ data: strat })
  client.put.mockRejectedValueOnce(new Error('Server error'))

  renderEdicion()

  await waitFor(() => screen.getByDisplayValue('Fender Stratocaster'))

  await userEvent.click(screen.getByRole('button', { name: /guardar cambios/i }))

  await waitFor(() =>
    expect(screen.getByText(/error al actualizar/i)).toBeInTheDocument()
  )
})
